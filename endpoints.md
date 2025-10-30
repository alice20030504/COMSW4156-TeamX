## Non-API Endpoints

| Method | Endpoint | Required Inputs | Optional Inputs |
|---|---|---|---|
| GET | `/` | None | None |
| GET | `/health` | None | None |

## Person APIs (`X-Client-ID` must start with `mobile-`)

| Method | Endpoint | Required Inputs | Optional Inputs |
|---|---|---|---|
| POST | `/api/persons` | Header: `X-Client-ID`; Body: `name`, `weight`, `height`, `birthDate (YYYY-MM-DD)` | Body: `id` (ignored), `clientId` (server sets) |
| PUT | `/api/persons/{id}` | Header: `X-Client-ID`; Path: `id`; Query: `birthDate (YYYY-MM-DD)`; Body: `name`, `weight`, `height`, `birthDate (YYYY-MM-DD)` | Body: `id` (overridden), `clientId` (overridden) |
| GET | `/api/persons/{id}` | Header: `X-Client-ID`; Path: `id`; Query: `birthDate (YYYY-MM-DD)` | None |
| DELETE | `/api/persons/{id}` | Header: `X-Client-ID`; Path: `id`; Query: `birthDate (YYYY-MM-DD)` | None |
| GET | `/api/persons` | Header: `X-Client-ID` | None |
| GET | `/api/persons/health` | Header: `X-Client-ID` | None |
| GET | `/api/persons/calories` | Header: `X-Client-ID`; Query: `weight (Double, kg)`, `height (Double, cm)`, `age (Integer, years)`, `gender (String)`, `weeklyTrainingFreq (Integer)` | None |
| GET | `/api/persons/bmi` | Header: `X-Client-ID`; Query: `weight (Double, kg)`, `height (Double, cm)` | None |

## Research APIs (`X-Client-ID` must start with `research-`)

| Method | Endpoint | Required Inputs | Optional Inputs |
|---|---|---|---|
| GET | `/api/research/persons` | Header: `X-Client-ID` | None |
| GET | `/api/research/demographics` | Header: `X-Client-ID` | Query: `ageRange`, `gender`, `objective` |
| GET | `/api/research/workout-patterns` | Header: `X-Client-ID` | Query: `ageRange` |
| GET | `/api/research/nutrition-trends` | Header: `X-Client-ID` | Query: `objective` (BULK, CUT, others default) |
| GET | `/api/research/population-health` | Header: `X-Client-ID` | None |

## Invalid Partitions (Possibly not covered yet)
| **Method** | **Endpoint** | **Invalid Partitions** |
|-------------|---------------|--------------------------|
| **GET** | `/` | None |
| **GET** | `/health` | None |
| **POST** | `/api/persons` | - Missing/invalid `X-Client-ID`<br>- Malformed or missing JSON body<br>- Missing/null/invalid field in JSON (`name`, `weight`, `height`, `birthDate`)<br>- `birthDate` wrong format<br>- Negative/zero weight/height (illogical values)<br>- Extra/unexpected fields<br>- DB/system error |
| **PUT** | `/api/persons/{id}` | - Missing/invalid `X-Client-ID`<br>- Path `id` not a number<br>- Query `birthDate` missing/invalid/wrong format<br>- Malformed/missing JSON body<br>- Missing/null/invalid body fields<br>- Extra/unexpected fields<br>- DB/system error |
| **GET** | `/api/persons/{id}` | - Missing/invalid `X-Client-ID`<br>- Path `id` not a number<br>- Query param `birthDate` missing/invalid/wrong format<br>- DB/system error |
| **DELETE** | `/api/persons/{id}` | - Missing/invalid `X-Client-ID`<br>- Path `id` not a number<br>- Query param `birthDate` missing/invalid/wrong format<br>- DB/system error |
| **GET** | `/api/persons` | - Missing/invalid `X-Client-ID`<br>- DB/system error |
| **GET** | `/api/persons/health` | - Missing/invalid `X-Client-ID` |
| **GET** | `/api/persons/calories` | - Missing/invalid `X-Client-ID`<br>- Missing/invalid parameter (`weight`, `height`, `age`, `gender`, `weeklyTrainingFreq`)<br>- Wrong type (e.g., string for integer param)<br>- Negative/zero/illogical values |
| **GET** | `/api/persons/bmi` | - Missing/invalid `X-Client-ID`<br>- Missing/invalid parameter (`weight` or `height`)<br>- Wrong type value<br>- Zero/negative/illogical values |
| **GET / other** | `/api/research/*` | - Missing/invalid `X-Client-ID`<br>- `X-Client-ID` not starting with `research-`<br>- Optional query params in wrong type/format |