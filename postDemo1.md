Here’s a PowerShell-friendly one-liner you can drop straight into your terminal:

using git bash

Mobile Controller Endpoints:

curl -X POST http://localhost:8080/api/persons \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice","weight":60.0,"height":50.0,"birthDate":"1998-05-06","goal":"BULK","gender":"feMALE"}'


curl -X POST http://localhost:8080/api/persons/plan \
  -H "Content-Type: application/json" \
  -H "X-Client-ID: mobile-id3" \
  -d '{"targetChangeKg":4.0,"durationWeeks":10,"trainingFrequencyPerWeek":5,"planStrategy":"BOTH"}'


curl.exe "http://localhost:8080/api/persons/me" -H "X-Client-ID: mobile-id3"

curl http://localhost:8080/api/persons/calories -H "X-Client-ID: mobile-id3"

curl http://localhost:8080/api/persons/recommendation -H "X-Client-ID: mobile-id1"



Research Controller Endpoints:

curl -X GET http://localhost:8080/api/research/demographics \
  -H "X-Client-ID: research-tool1"


  # Population health snapshot (succeeds once CUT and BULK profiles exist)
curl -X GET http://localhost:8080/api/research/population-health \
  -H "X-Client-ID: research-tool1"