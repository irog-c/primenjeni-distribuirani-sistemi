import requests
import json

url = "https://getpantry.cloud/apiv1/pantry/3ab3757e-2586-4248-8cd5-843b30ae8ab8/basket/igor_mihajlov_rm5316"

payload = json.dumps({
  "id": "123abc",
  "ime": "Igor",
  "prezime": "Igoric",
  "smer": "RM",
  "predmeti": [
    "Matematika",
    "Engleski",
    "Programiranje"
  ],
  "prosek": 6,
  "kontakt": {
    "adresa": "Bulevar 1/3",
    "mesto": "Beograd",
    "telefon": "+381 69 222 22 22"
  }
})
headers = {
  'Content-Type': 'application/json'
}

response = requests.request("POST", url, headers=headers, data=payload)

print(response.text)
