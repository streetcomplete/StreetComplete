import os
import re

source_dir = "src/main/res/"
target_dir = "../fastlane/metadata/android/"

def copy_key_from_strings_xml_to_file(xml, key, filename):
	match = re.search("<string name=\"" + key + "\">\"?(.*?)\"?</string>", xml, re.DOTALL)
	
	if match:
		with open(filename, "w", encoding='utf8') as file:
			file.write(match.group(1))

def get_locale_from(dirname):
	if not dirname.startswith("values"):
		return None
	components = dirname.split("-")
	if len(components) == 1:
		return "en"
	elif re.search('[0-9]',components[1]):
		return None
	elif len(components) == 2:
		return components[1]
	elif len(components) == 3:
		return components[1] + "-" + components[2][1:]
	return None

for dirname in sorted(os.listdir(source_dir)):
	locale = get_locale_from(dirname)
	if not locale:
		continue
	
	stringsfile = source_dir + dirname + "/strings.xml"
	if not os.path.exists(stringsfile):
		continue;
	
	print(locale)
	
	locale_dir = target_dir + locale
	if not os.path.exists(locale_dir):
		os.makedirs(locale_dir)
	
	with open(stringsfile, 'r', encoding='utf8') as file:
		xml = file.read()
		copy_key_from_strings_xml_to_file(xml, "store_listing_short_description", locale_dir + "/short_description.txt")
		copy_key_from_strings_xml_to_file(xml, "store_listing_full_description", locale_dir + "/full_description.txt")
