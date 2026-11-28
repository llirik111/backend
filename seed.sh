BASE_URL="http://localhost:8080"

curl -s -X POST "${BASE_URL}/api/clients" \
  -H "Content-Type: application/json; charset=utf-8" \
  -d '{"fullName":"Alexey Smirnov","phone":"+79140010101","email":"alexey.smirnov@mail.ru","birthDate":"1994-03-12"}'

curl -s -X POST "${BASE_URL}/api/clients" \
  -H "Content-Type: application/json; charset=utf-8" \
  -d '{"fullName":"Ekaterina Kuznetsova","phone":"+79140020202","email":"katya.kuznetsova@gmail.com","birthDate":"1999-07-25"}'

curl -s -X POST "${BASE_URL}/api/clients" \
  -H "Content-Type: application/json; charset=utf-8" \
  -d '{"fullName":"Dmitry Sokolov","phone":"+79140030303","email":"dmitry.sokolov@yandex.ru","birthDate":"1988-11-04"}'

curl -s -X POST "${BASE_URL}/api/clients" \
  -H "Content-Type: application/json; charset=utf-8" \
  -d '{"fullName":"Olga Morozova","phone":"+79140040404","email":"olga.morozova@inbox.ru","birthDate":"2001-05-18"}'

curl -s -X POST "${BASE_URL}/api/clients" \
  -H "Content-Type: application/json; charset=utf-8" \
  -d '{"fullName":"Mikhail Vasiliev","phone":"+79140050505","email":"mikhail.v@corp.com","birthDate":"1992-09-30"}'

curl -s -X POST "${BASE_URL}/api/clients" \
  -H "Content-Type: application/json; charset=utf-8" \
  -d '{"fullName":"Anna Popova","phone":"+79140060606","email":"anna.popova@bk.ru","birthDate":"1997-12-08"}'

curl -s -X POST "${BASE_URL}/api/clients/6/block"

curl -s -X POST "${BASE_URL}/api/memberships" \
  -H "Content-Type: application/json" \
  -d '{"clientId":1,"type":"UNLIMITED","startDate":"2026-10-01","durationDays":30,"price":4500}'

curl -s -X POST "${BASE_URL}/api/memberships" \
  -H "Content-Type: application/json" \
  -d '{"clientId":2,"type":"VISIT_BASED","startDate":"2026-10-01","durationDays":60,"totalVisits":10,"price":3500}'

curl -s -X POST "${BASE_URL}/api/memberships" \
  -H "Content-Type: application/json" \
  -d '{"clientId":3,"type":"DAYTIME","startDate":"2026-10-01","durationDays":30,"price":3000}'

curl -s -X POST "${BASE_URL}/api/memberships" \
  -H "Content-Type: application/json" \
  -d '{"clientId":4,"type":"UNLIMITED","startDate":"2026-10-01","durationDays":60,"price":8000}'

curl -s -X POST "${BASE_URL}/api/memberships/4/freeze" \
  -H "Content-Type: application/json" \
  -d '{"days":14,"requestDate":"2026-10-15"}'

curl -s -X POST "${BASE_URL}/api/memberships" \
  -H "Content-Type: application/json" \
  -d '{"clientId":5,"type":"VISIT_BASED","startDate":"2026-10-01","durationDays":30,"totalVisits":1,"price":600}'

curl -s -X POST "${BASE_URL}/api/memberships/1/check-in" \
  -H "Content-Type: application/json" \
  -d '{"preferredLocker":"L-101"}'

curl -s -X POST "${BASE_URL}/api/memberships/2/check-in" \
  -H "Content-Type: application/json" \
  -d '{"preferredLocker":"L-102"}'

curl -s -X POST "${BASE_URL}/api/visits/2/check-out" \
  -H "Content-Type: application/json" \
  -d '{"exitTime":"2026-10-29T23:59:00"}'
