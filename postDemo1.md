Here’s a PowerShell-friendly one-liner you can drop straight into your terminal:

using git bash

1.
curl -X POST http://localhost:8080/api/persons \
  -H "Content-Type: application/json" \
  -d '{"name":"Rr Example","weight":80.0,"height":180.0,"birthDate":"1999-05-06","goal":"CUT","gender":"MALE"}'


2.
curl.exe "http://localhost:8080/api/persons/me" -H "X-Client-ID: mobile-id1"