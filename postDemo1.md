Here’s a PowerShell-friendly one-liner you can drop straight into your terminal:

using git bash

curl -X POST http://localhost:8080/api/persons \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice","weight":65.0,"height":190.0,"birthDate":"1991-05-06","goal":"BULK","gender":"feMALE"}'


curl -X POST http://localhost:8080/api/persons/plan \
  -H "Content-Type: application/json" \
  -H "X-Client-ID: mobile-id3" \
  -d '{"targetChangeKg":4.0,"durationWeeks":10,"trainingFrequencyPerWeek":5,"planStrategy":"BOTH"}'


curl.exe "http://localhost:8080/api/persons/me" -H "X-Client-ID: mobile-id3"

curl http://localhost:8080/api/persons/calories -H "X-Client-ID: mobile-id3"

curl http://localhost:8080/api/persons/recommendation -H "X-Client-ID: mobile-id1"