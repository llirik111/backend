#!/usr/bin/env bash

BASE_URL="http://localhost:8080"

echo "=== 1. Ping endpoint tests ==="
curl -i -X GET "${BASE_URL}/ping"
echo ""
curl -i -X POST "${BASE_URL}/ping"
echo ""

echo "=== 2. Create clients (POST /api/clients) ==="
curl -i -X POST "${BASE_URL}/api/clients" \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Ivan Ivanov","phone":"+79991112233","email":"ivan@example.com","birthDate":"1995-05-15"}'
echo ""

curl -i -X POST "${BASE_URL}/api/clients" \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Maria Petrova","phone":"+79992223344","email":"maria@example.com","birthDate":"1998-08-20"}'
echo ""

curl -i -X POST "${BASE_URL}/api/clients" \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Alexey Sidorov","phone":"+79993334455","email":"alex@example.com","birthDate":"2000-01-10"}'
echo ""

echo "=== 3. Validation and Conflict errors on client creation ==="
curl -i -X POST "${BASE_URL}/api/clients" \
  -H "Content-Type: application/json" \
  -d '{"fullName":"","phone":"+79990000000","email":"empty@example.com","birthDate":"1990-01-01"}'
echo ""

curl -i -X POST "${BASE_URL}/api/clients" \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Invalid Email","phone":"+79990000001","email":"not-an-email","birthDate":"1990-01-01"}'
echo ""

curl -i -X POST "${BASE_URL}/api/clients" \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Duplicate Phone","phone":"+79991112233","email":"other@example.com","birthDate":"1990-01-01"}'
echo ""

echo "=== 4. Get and filter clients (GET /api/clients) ==="
curl -i -X GET "${BASE_URL}/api/clients"
echo ""

curl -i -X GET "${BASE_URL}/api/clients?query=Petrova"
echo ""

curl -i -X GET "${BASE_URL}/api/clients/1"
echo ""

curl -i -X GET "${BASE_URL}/api/clients/999"
echo ""

echo "=== 5. Update, block, unblock and delete client ==="
curl -i -X PUT "${BASE_URL}/api/clients/1" \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Ivan Ivanov Updated","phone":"+79991112233","email":"ivan_new@example.com","birthDate":"1995-05-15"}'
echo ""

curl -i -X POST "${BASE_URL}/api/clients/3/block"
echo ""

curl -i -X POST "${BASE_URL}/api/clients/3/unblock"
echo ""

curl -i -X DELETE "${BASE_URL}/api/clients/3"
echo ""

curl -i -X GET "${BASE_URL}/api/clients/3"
echo ""

echo "=== 6. Create memberships (POST /api/memberships) ==="
curl -i -X POST "${BASE_URL}/api/memberships" \
  -H "Content-Type: application/json" \
  -d '{"clientId":1,"type":"UNLIMITED","startDate":"2026-10-01","durationDays":30,"price":4500.00}'
echo ""

curl -i -X POST "${BASE_URL}/api/memberships" \
  -H "Content-Type: application/json" \
  -d '{"clientId":2,"type":"VISIT_BASED","startDate":"2026-10-01","durationDays":30,"totalVisits":1,"price":1200.00}'
echo ""

curl -i -X POST "${BASE_URL}/api/memberships" \
  -H "Content-Type: application/json" \
  -d '{"clientId":1,"type":"DAYTIME","startDate":"2026-10-01","durationDays":30,"price":3000.00}'
echo ""

echo "=== 7. Membership validation errors ==="
curl -i -X POST "${BASE_URL}/api/memberships" \
  -H "Content-Type: application/json" \
  -d '{"clientId":2,"type":"VISIT_BASED","startDate":"2026-10-01","durationDays":30,"price":1000.00}'
echo ""

curl -i -X POST "${BASE_URL}/api/memberships" \
  -H "Content-Type: application/json" \
  -d '{"clientId":999,"type":"UNLIMITED","startDate":"2026-10-01","durationDays":30,"price":1000.00}'
echo ""

curl -i -X POST "${BASE_URL}/api/memberships" \
  -H "Content-Type: application/json" \
  -d '{"clientId":1,"type":"UNLIMITED","startDate":"2026-10-01","durationDays":30,"price":-500.00}'
echo ""

echo "=== 8. Get and filter memberships (GET /api/memberships) ==="
curl -i -X GET "${BASE_URL}/api/memberships"
echo ""

curl -i -X GET "${BASE_URL}/api/memberships?clientId=1"
echo ""

curl -i -X GET "${BASE_URL}/api/memberships?type=VISIT_BASED"
echo ""

curl -i -X GET "${BASE_URL}/api/memberships/1"
echo ""

echo "=== 9. Domain operation: Freeze and Unfreeze memberships ==="
curl -i -X POST "${BASE_URL}/api/memberships/1/freeze" \
  -H "Content-Type: application/json" \
  -d '{"days":10,"requestDate":"2026-10-15"}'
echo ""

curl -i -X POST "${BASE_URL}/api/memberships/1/freeze" \
  -H "Content-Type: application/json" \
  -d '{"days":5,"requestDate":"2026-10-16"}'
echo ""

curl -i -X POST "${BASE_URL}/api/memberships/2/freeze" \
  -H "Content-Type: application/json" \
  -d '{"days":7,"requestDate":"2026-10-15"}'
echo ""

curl -i -X POST "${BASE_URL}/api/memberships/1/check-in" \
  -H "Content-Type: application/json" \
  -d '{"preferredLocker":"L-105"}'
echo ""

curl -i -X POST "${BASE_URL}/api/memberships/1/unfreeze" \
  -H "Content-Type: application/json" \
  -d '{"requestDate":"2026-10-25"}'
echo ""

curl -i -X POST "${BASE_URL}/api/memberships/1/unfreeze" \
  -H "Content-Type: application/json" \
  -d '{"requestDate":"2026-10-26"}'
echo ""

echo "=== 10. Domain operation: Check-in, Visits, and Lockers ==="
curl -i -X POST "${BASE_URL}/api/memberships/1/check-in" \
  -H "Content-Type: application/json" \
  -d '{"preferredLocker":"L-101"}'
echo ""

curl -i -X POST "${BASE_URL}/api/memberships/1/check-in" \
  -H "Content-Type: application/json" \
  -d '{"preferredLocker":"L-102"}'
echo ""

curl -i -X POST "${BASE_URL}/api/memberships/1/freeze" \
  -H "Content-Type: application/json" \
  -d '{"days":5,"requestDate":"2026-10-28"}'
echo ""

curl -i -X DELETE "${BASE_URL}/api/clients/1"
echo ""

curl -i -X POST "${BASE_URL}/api/memberships/2/check-in" \
  -H "Content-Type: application/json" \
  -d '{"preferredLocker":"L-101"}'
echo ""

curl -i -X POST "${BASE_URL}/api/memberships/2/check-in" \
  -H "Content-Type: application/json" \
  -d '{}'
echo ""

curl -i -X GET "${BASE_URL}/api/memberships/2"
echo ""

curl -i -X POST "${BASE_URL}/api/memberships/2/check-in" \
  -H "Content-Type: application/json" \
  -d '{}'
echo ""

echo "=== 11. Daytime membership time restriction check ==="
curl -i -X POST "${BASE_URL}/api/memberships/3/check-in" \
  -H "Content-Type: application/json" \
  -d '{"visitTime":"2026-10-20T21:00:00"}'
echo ""

echo "=== 12. Visits listing and Check-out ==="
curl -i -X GET "${BASE_URL}/api/visits?status=IN_PROGRESS"
echo ""

curl -i -X GET "${BASE_URL}/api/visits/1"
echo ""

curl -i -X POST "${BASE_URL}/api/visits/1/check-out" \
  -H "Content-Type: application/json" \
  -d '{"exitTime":"2026-10-29T21:30:00"}'
echo ""

curl -i -X POST "${BASE_URL}/api/visits/1/check-out" \
  -H "Content-Type: application/json" \
  -d '{"exitTime":"2026-10-29T21:35:00"}'
echo ""

echo "=== 13. Cancellation, Membership deletion and cleanup ==="
curl -i -X POST "${BASE_URL}/api/memberships/1/cancel"
echo ""

curl -i -X POST "${BASE_URL}/api/memberships/1/check-in" \
  -H "Content-Type: application/json" \
  -d '{}'
echo ""

curl -i -X DELETE "${BASE_URL}/api/memberships/3"
echo ""

curl -i -X GET "${BASE_URL}/api/memberships/3"
echo ""

echo "=== 14. HTTP error handling and resilience ==="
curl -i -X PUT "${BASE_URL}/ping"
echo ""

curl -i -X POST "${BASE_URL}/api/clients/1"
echo ""

curl -i -X POST "${BASE_URL}/api/clients" \
  -H "Content-Type: application/json" \
  -d '{"fullName": "Broken Json'
echo ""

curl -i -X GET "${BASE_URL}/api/non-existent-route"
echo ""
