import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Command-line demo client for the Personal Fitness Management Service.
 *
 * <p>Supports key workflows for mobile clients:
 * <ul>
 *   <li>registering a new profile (obtain X-Client-ID)</li>
 *   <li>configuring goal plans</li>
 *   <li>retrieving profile, BMI, calorie, and recommendation data</li>
 *   <li>listing all profiles tied to the client identifier</li>
 * </ul>
 *
 * <p>Run with {@code java DemoClient <command> [--option value]}.
 * Use {@code --base-url} (defaults to {@code http://localhost:8080}) and
 * {@code --client-id} or the {@code FITNESS_CLIENT_ID} environment variable for authenticated calls.
 *
 * <p>Examples:
 *
 * <pre>
 * java DemoClient register --name "Ava Stone" --weight 68.5 --height 172 \
 *     --birth-date 1995-03-18 --goal CUT --gender FEMALE
 *
 * java DemoClient --client-id mobile-id1 plan \
 *     --target-change 3.5 --duration-weeks 6 --training-frequency 4 --plan-strategy BOTH
 *
 * java DemoClient --client-id mobile-id1 bmi
 * </pre>
 *
 * <p>Run {@code java DemoClient demo} for a guided workflow. Launch multiple instances by supplying unique
 * client identifiers to simulate concurrent clients; the service isolates requests via the {@code X-Client-ID} header.
 */
public final class DemoClient {

  private static final String DEFAULT_BASE_URL =
      Optional.ofNullable(System.getenv("FITNESS_API_URL")).filter(s -> !s.isBlank())
          .orElse("http://localhost:8080");

  private static final String ENV_CLIENT_ID = "FITNESS_CLIENT_ID";
  private static final int DEFAULT_TIMEOUT_SECONDS = 15;

  private final HttpClient httpClient;
  private final String baseUrl;
  private final String clientId;
  private final Duration timeout;

  private DemoClient(String baseUrl, String clientId, Duration timeout) {
    this.httpClient = HttpClient.newBuilder().connectTimeout(timeout).build();
    this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    this.clientId = clientId;
    this.timeout = timeout;
  }

  public static void main(String[] args) {
    if (args.length == 0) {
      printUsage();
      return;
    }

    List<String> argList = new ArrayList<>(Arrays.asList(args));
    String command = argList.remove(0).toLowerCase(Locale.ROOT);
    ParsedArgs parsedArgs = parseArgs(argList);

    String baseUrl = parsedArgs.options.getOrDefault("base-url", DEFAULT_BASE_URL);
    String cliClientId = parsedArgs.options.get("client-id");
    if (cliClientId == null || cliClientId.isBlank()) {
      cliClientId = Optional.ofNullable(System.getenv(ENV_CLIENT_ID)).orElse(null);
    }
    int timeoutSeconds = Integer.parseInt(parsedArgs.options.getOrDefault(
        "timeout", String.valueOf(DEFAULT_TIMEOUT_SECONDS)));
    DemoClient client = new DemoClient(baseUrl, cliClientId, Duration.ofSeconds(timeoutSeconds));

    try {
      switch (command) {
        case "register":
          client.register(parsedArgs.options);
          break;
        case "plan":
          client.plan(parsedArgs.options);
          break;
        case "profile":
          client.simpleGet("/api/persons/me", true);
          break;
        case "list":
          client.simpleGet("/api/persons", true);
          break;
        case "bmi":
          client.simpleGet("/api/persons/bmi", true);
          break;
        case "calories":
          client.simpleGet("/api/persons/calories", true);
          break;
        case "recommendation":
          client.simpleGet("/api/persons/recommendation", true);
          break;
        case "health":
          client.simpleGet("/api/persons/health", false);
          break;
        case "demo":
          client.runDemo(parsedArgs.options);
          break;
        default:
          System.err.println("Unknown command: " + command);
          printUsage();
      }
    } catch (IllegalArgumentException e) {
      System.err.println("Validation error: " + e.getMessage());
      System.exit(1);
    } catch (IOException | InterruptedException e) {
      System.err.println("Request failed: " + e.getMessage());
      System.exit(1);
    } catch (ApiException e) {
      System.err.println("API error (" + e.statusCode + "): " + e.responseBody);
      System.exit(1);
    }
  }

  private static void printUsage() {
    System.out.println("""
        Usage: java DemoClient <command> [options]

        Commands:
          register         Register a new profile; returns generated clientId.
          plan             Store goal plan metadata for the active client.
          profile          Retrieve the current client profile.
          list             List all persons associated with the client.
          bmi              Compute BMI using stored metrics.
          calories         Retrieve calorie guidance (requires plan data).
          recommendation   Fetch motivational recommendation details.
          health           Read service health (no client id required).
          demo             Run an end-to-end workflow (register/plan/summaries).

        Common options:
          --base-url <url>       Override API base URL (default: http://localhost:8080).
          --client-id <id>       Supply X-Client-ID header (defaults to env FITNESS_CLIENT_ID).
          --timeout <seconds>    HTTP timeout (default: 15).

        Run `java DemoClient demo` to see the workflow in action.
        """);
  }

  private static ParsedArgs parseArgs(List<String> tokens) {
    Map<String, String> options = new LinkedHashMap<>();
    List<String> positionals = new ArrayList<>();
    Iterator<String> iterator = tokens.iterator();
    while (iterator.hasNext()) {
      String token = iterator.next();
      if (token.startsWith("--")) {
        String withoutPrefix = token.substring(2);
        String key;
        String value;
        int equalsIndex = withoutPrefix.indexOf('=');
        if (equalsIndex >= 0) {
          key = withoutPrefix.substring(0, equalsIndex);
          value = withoutPrefix.substring(equalsIndex + 1);
        } else {
          key = withoutPrefix;
          if (iterator.hasNext()) {
            value = iterator.next();
          } else {
            value = "";
          }
        }
        options.put(key.toLowerCase(Locale.ROOT), value);
      } else {
        positionals.add(token);
      }
    }
    return new ParsedArgs(options, positionals);
  }

  private void register(Map<String, String> options) throws IOException, InterruptedException {
    ensurePresent(options, "name");
    ensurePresent(options, "weight");
    ensurePresent(options, "height");
    ensurePresent(options, "birth-date");
    ensurePresent(options, "goal");
    ensurePresent(options, "gender");

    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("name", options.get("name"));
    payload.put("weight", Double.parseDouble(options.get("weight")));
    payload.put("height", Double.parseDouble(options.get("height")));
    payload.put("birthDate", options.get("birth-date"));
    payload.put("goal", options.get("goal").toUpperCase(Locale.ROOT));
    payload.put("gender", options.get("gender").toUpperCase(Locale.ROOT));

    String response = callApi("POST", "/api/persons", payload, false);
    System.out.println("Registration succeeded. Response:");
    System.out.println(response);
  }

  private void plan(Map<String, String> options) throws IOException, InterruptedException {
    requireClientId();
    ensurePresent(options, "target-change");
    ensurePresent(options, "duration-weeks");
    ensurePresent(options, "training-frequency");
    ensurePresent(options, "plan-strategy");

    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("targetChangeKg", Double.parseDouble(options.get("target-change")));
    payload.put("durationWeeks", Integer.parseInt(options.get("duration-weeks")));
    payload.put("trainingFrequencyPerWeek", Integer.parseInt(options.get("training-frequency")));
    payload.put("planStrategy", options.get("plan-strategy").toUpperCase(Locale.ROOT));

    String response = callApi("POST", "/api/persons/plan", payload, true);
    System.out.println("Plan stored. Response:");
    System.out.println(response);
  }

  private void simpleGet(String path, boolean requireClient)
      throws IOException, InterruptedException {
    String response = callApi("GET", path, null, requireClient);
    System.out.println(response);
  }

  private void runDemo(Map<String, String> options) throws IOException, InterruptedException {
    DemoClient workingClient = this;
    if (this.clientId == null || this.clientId.isBlank()) {
      System.out.println("No client id supplied; registering a demo profile...");
      Map<String, String> registerOpts = new HashMap<>();
      registerOpts.put("name", options.getOrDefault("name", "Demo User"));
      registerOpts.put("weight", options.getOrDefault("weight", "72.0"));
      registerOpts.put("height", options.getOrDefault("height", "178.0"));
      registerOpts.put("birth-date", options.getOrDefault("birth-date", "1994-05-21"));
      registerOpts.put("goal", options.getOrDefault("goal", "CUT"));
      registerOpts.put("gender", options.getOrDefault("gender", "MALE"));

      Map<String, Object> payload = new LinkedHashMap<>();
      payload.put("name", registerOpts.get("name"));
      payload.put("weight", Double.parseDouble(registerOpts.get("weight")));
      payload.put("height", Double.parseDouble(registerOpts.get("height")));
      payload.put("birthDate", registerOpts.get("birth-date"));
      payload.put("goal", registerOpts.get("goal").toUpperCase(Locale.ROOT));
      payload.put("gender", registerOpts.get("gender").toUpperCase(Locale.ROOT));

      String response = callApi("POST", "/api/persons", payload, false);
      System.out.println("Registration response:");
      System.out.println(response);
      String generatedId = extractClientId(response);
      if (generatedId == null) {
        throw new IllegalArgumentException("Registration response did not include clientId.");
      }
      workingClient = new DemoClient(this.baseUrl, generatedId, this.timeout);
      System.out.println("Using generated clientId: " + generatedId);
    } else {
      System.out.println("Using supplied clientId: " + this.clientId);
    }

    Map<String, String> planOpts = new HashMap<>();
    planOpts.put("target-change", options.getOrDefault("target-change", "4.0"));
    planOpts.put("duration-weeks", options.getOrDefault("duration-weeks", "8"));
    planOpts.put("training-frequency", options.getOrDefault("training-frequency", "4"));
    planOpts.put("plan-strategy", options.getOrDefault("plan-strategy", "BOTH"));
    workingClient.plan(planOpts);

    System.out.println("Profile snapshot:");
    workingClient.simpleGet("/api/persons/me", true);
    System.out.println("BMI summary:");
    workingClient.simpleGet("/api/persons/bmi", true);
    System.out.println("Calorie guidance:");
    workingClient.simpleGet("/api/persons/calories", true);
    System.out.println("Recommendation:");
    workingClient.simpleGet("/api/persons/recommendation", true);
  }

  private void requireClientId() {
    if (clientId == null || clientId.isBlank()) {
      throw new IllegalArgumentException(
          "Client id is required. Provide --client-id or set " + ENV_CLIENT_ID + ".");
    }
  }

  private String callApi(
      String method, String path, Map<String, Object> jsonBody, boolean requireClientHeader)
      throws IOException, InterruptedException {
    if (requireClientHeader) {
      requireClientId();
    }
    HttpRequest.Builder builder = HttpRequest.newBuilder()
        .timeout(this.timeout)
        .uri(URI.create(this.baseUrl + path));
    if (requireClientHeader) {
      builder.header("X-Client-ID", Objects.requireNonNull(this.clientId));
    }
    if (jsonBody != null) {
      builder.header("Content-Type", "application/json");
      builder.method(method, HttpRequest.BodyPublishers.ofString(toJson(jsonBody)));
    } else {
      builder.method(method, HttpRequest.BodyPublishers.noBody());
    }

    HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    if (response.statusCode() >= 200 && response.statusCode() < 300) {
      return response.body();
    }
    throw new ApiException(response.statusCode(), response.body());
  }

  private static void ensurePresent(Map<String, String> options, String key) {
    if (!options.containsKey(key) || options.get(key).isBlank()) {
      throw new IllegalArgumentException("Missing required option --" + key);
    }
  }

  private static String toJson(Map<String, Object> map) {
    StringBuilder sb = new StringBuilder();
    sb.append("{");
    Iterator<Map.Entry<String, Object>> iterator = map.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry<String, Object> entry = iterator.next();
      sb.append("\"").append(escape(entry.getKey())).append("\":");
      sb.append(valueToJson(entry.getValue()));
      if (iterator.hasNext()) {
        sb.append(",");
      }
    }
    sb.append("}");
    return sb.toString();
  }

  private static String valueToJson(Object value) {
    if (value == null) {
      return "null";
    }
    if (value instanceof Number || value instanceof Boolean) {
      return value.toString();
    }
    if (value instanceof Map) {
      @SuppressWarnings("unchecked") Map<String, Object> nested = (Map<String, Object>) value;
      return toJson(nested);
    }
    if (value instanceof Collection) {
      StringBuilder sb = new StringBuilder();
      sb.append("[");
      Iterator<?> iterator = ((Collection<?>) value).iterator();
      while (iterator.hasNext()) {
        sb.append(valueToJson(iterator.next()));
        if (iterator.hasNext()) {
          sb.append(",");
        }
      }
      sb.append("]");
      return sb.toString();
    }
    return "\"" + escape(value.toString()) + "\"";
  }

  private static String escape(String value) {
    StringBuilder sb = new StringBuilder();
    for (char c : value.toCharArray()) {
      switch (c) {
        case '\\':
        case '"':
          sb.append('\\').append(c);
          break;
        case '\b':
          sb.append("\\b");
          break;
        case '\f':
          sb.append("\\f");
          break;
        case '\n':
          sb.append("\\n");
          break;
        case '\r':
          sb.append("\\r");
          break;
        case '\t':
          sb.append("\\t");
          break;
        default:
          if (c < 0x20) {
            sb.append(String.format("\\u%04x", (int) c));
          } else {
            sb.append(c);
          }
      }
    }
    return sb.toString();
  }

  private static String extractClientId(String responseBody) {
    // simple heuristic: look for "clientId":"value"
    String marker = "\"clientId\"";
    int markerIndex = responseBody.indexOf(marker);
    if (markerIndex < 0) {
      return null;
    }
    int colonIndex = responseBody.indexOf(':', markerIndex);
    if (colonIndex < 0) {
      return null;
    }
    int startQuote = responseBody.indexOf('"', colonIndex + 1);
    if (startQuote < 0) {
      return null;
    }
    int endQuote = responseBody.indexOf('"', startQuote + 1);
    if (endQuote < 0) {
      return null;
    }
    return responseBody.substring(startQuote + 1, endQuote);
  }

  private static final class ParsedArgs {
    private final Map<String, String> options;
    private final List<String> positionals;

    private ParsedArgs(Map<String, String> options, List<String> positionals) {
      this.options = options;
      this.positionals = positionals;
    }
  }

  private static final class ApiException extends RuntimeException {
    private final int statusCode;
    private final String responseBody;

    private ApiException(int statusCode, String responseBody) {
      super("HTTP " + statusCode);
      this.statusCode = statusCode;
      this.responseBody = responseBody;
    }
  }
}

