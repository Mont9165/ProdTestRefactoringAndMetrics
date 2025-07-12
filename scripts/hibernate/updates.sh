POM=$(ls |grep .jar|awk '{ print $1 }')
sed -i '' 's/META-INF\/.*\.jar/META-INF\/'$POM'/g' 'persistence.xml'
cd ../../../..
mvn clean install -U
