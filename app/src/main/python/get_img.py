import requests
import sys

subscription_key = "your-subscription-key"
search_url = "https://api.cognitive.microsoft.com/bing/v7.0/images/search"
search_term = sys.argv[0]

headers = {"Ocp-Apim-Subscription-Key" : subscription_key}
params  = {"q": search_term, "license": "public", "imageType": "photo"}

response = requests.get(search_url, headers=headers, params=params)
response.raise_for_status()
search_results = response.json()

thumbnail_urls = [img["thumbnailUrl"] for img in search_results["value"][:16]]

print(thumbnail_urls[0])
