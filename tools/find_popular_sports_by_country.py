import collections
import urllib.request
import urllib.parse
import sys

languageCode = sys.argv[1]
query = "[timeout:3600][out:csv(sport)];(area[\"ISO3166-1\"="+languageCode+"];)->.x;way[leisure=pitch][sport](area.x);out tags;"
encodedQueryData = str.encode(urllib.parse.urlencode({ "data" : query }))
response = urllib.request.urlopen("https://overpass-api.de/api/interpreter", encodedQueryData, 3600)
dict = {}
for line in response:
	line = line.decode("utf-8").rstrip()
	if not(line in dict):
		dict[line] = 1
	else:
		dict[line] = dict[line]+1
for key,value in sorted(dict.items(), key=lambda x: x[1], reverse=True):
	print(str(value)+"\t"+key)
