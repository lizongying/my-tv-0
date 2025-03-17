.PHONY: gen-version

all:  gen-version

gen-version:
	git describe --tags --always
	git describe --tags --always | sed 's/v/ /g' | sed 's/\./ /g' | sed 's/-/ /g' | awk '{print ($$1*16777216)+($$2*65536)+($$3*256)+$$4}'

#make gen v=1.2.3
gen:
	echo $(v) | sed 's/v/ /g' | sed 's/\./ /g' | sed 's/-/ /g' | awk '{print "{\"version_code\": " ($$1*16777216)+($$2*65536)+($$3*256)+$$4 ", \"version_name\": \"" "v$(v)" "\", \"apk_name\": \"" "my-tv-0_$(v).apk" "\", \"apk_url\": \"" "https://www.gitlink.org.cn/lizongying/my-tv-0/releases/download/v$(v)/my-tv-0_$(v).apk" "\"}"}' > version.json
