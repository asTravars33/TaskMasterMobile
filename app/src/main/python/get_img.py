import requests
import sys
import os

def get_image(search_term):
    #subscription_key = "ab03cfba11cc41949b26b6ba22383ebc"
    key = os.environ.get('AZURE_SEARCH_KEY', '97a7c968928c40aeab8e2bc19550efff')
    search_url = "https://api.bing.microsoft.com/v7.0/images/search"
    #search_url = "https://api.cognitive.microsoft.com/bing/v7.0/images/search"

    headers = {"Ocp-Apim-Subscription-Key" : key}
    #params  = {"q": search_term, "license": "public", "imageType": "photo"}
    params = {'q':search_term, 'count':2, 'min_height':128, 'min_width':128}

    response = requests.get(search_url, headers=headers, params=params)
    response.raise_for_status()
    search_results = response.json()

    thumbnail_urls = [img["thumbnailUrl"] for img in search_results["value"][:16]]

    return thumbnail_urls[0]
    '''key = os.environ.get('AZURE_SEARCH_KEY', 'ab03cfba11cc41949b26b6ba22383ebc')
    results = search_images_bing(key, f'{search_term}')
    return results.attrgot('contentUrl')[0]'''
