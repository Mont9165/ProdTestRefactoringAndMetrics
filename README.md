<H1>Replication package of "How Does Test Code Differ From Production Code
in Terms of Refactoring? An Empirical Study"</H1>

This repository includes the replication package including the source code and results of [the paper](How%20Does%20Test%20Code%20Differ%20From%20Production%20Code%20in%20Terms%20of%20Refactoring%3F%20An%20Empirical%20Study.pdf).
### Data
[Data collection](result/data_collection/DataCollection.csv) <br>
[RQ1 frequency](result/rq1/frequency) <br>
[RQ1 types](result/rq1/types) <br>
[RQ2 code metrics](result/rq2/code_metrics) <br>
[RQ2 code smell](result/rq2/code_smell) <br>
[RQ2 readability](result/rq2/readability) <br>

## Require
- JDK 17
- Git (>2.24)
- Maven (>3.6.1)
- Postgres (>12.1)


## Settings (for Mac or Linux)

If you have any problems about settings, you can refer to TraceCollector project. 

**1. Install JDK on your local**

**2. Set JAVA Environment (JAVA_HOME)**


**3. Set Maven Environment Variables (M2_HOME)**

- e.g., export M2_HOME=/usr/local/Cellar/maven/3.6.3_1/

**4. Add the following setting in your ~/.m2/settings.xml**
```angular2html
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://mave/
n.apache.org/xsd/settings-1.0.0.xsd">
<servers>
	<server>
		<id>kashiwa</id>
		<username>YOUR_USERNAME</username>
		<password>YOUR_ACCESS_TOKEN</password>
	</server>
</servers>
</settings>
```



**3. create ini files for your computer (optional)**

- settings/base.ini
	- data_dir=[directory where you want to put the output]
- settings/projects/[your project name].pj
	- name=[your project name]
	- abb=[your project abbr name]
	- url=[url of your project repository]



## How to run
Run the following shell scripts on Server or local.
To run on docker, you need the following four steps (If you want to run it on local, go to On local):


```angular2html
git clone "THIS REPOSITORY URL"
cd THIS_REPOSITORY_NAME
mvn clean install -DskipTests
```

### On Singularity
#### To get refactoring data and metrics from the projects, you can run the following commands on Server.
**1. Make a docker image and push it to DockerHub on your local (the image should be public)**
```
docker login
docker buildx create --use
docker buildx build --platform linux/amd64 -t your_dockerhub_username/test-refactoring-analysis:tag --push .
```

**2. Data collection**
```
module load singularity
singularity build test-refactoring-analysis.sif docker://your_dockerhub_username/test-refactoring-analysis:tag
sbatch scripts/sh/collect_refactoring_parallel.sh
sbatch scripts/sh/collect-metrics-for-refactoring_parallel.sh
git clone https://github.com/Mont9165/DesigniteRunner
# Follow the instructions in the DesigniteRunner repository to run the code smells & code metrics analysis
cd DesigniteRunner
sbatch 1_sbatch_make-list.sh
sbatch 2_sbatch_main.sh
```

**Get Results csv from DB** <br>
```
# commits
psql -h HOST_NAME -p 5432 -U USER_NAME -d DB_NAME -f scripts/sql/result/rq1/analyze_refactoring_activity_by_project.sql > scripts/sql/result/rq1/data/refactoring_activity_by_project.csv
# refactoring details
psql -h HOST_NAME -p 5432 -U USER_NAME -d DB_NAME -f scripts/sql/result/rq1/refactoring_details.sql > scripts/sql/result/rq1/data/refactoring_details.csv
```
Finished data collection, you can run the following commands to get the results of RQ1 and RQ2.

**RQ1** <br>
```angular2html
python -m src.rq1_freq.main
python -m src.rq1_type.graph

```

**RQ2** <br>
```angular2html
python -m src.preprocess_data
python -m src.run_analysis
python -m src.rq2_smell_metrics.main
python -m src.rq2_smell_metrics.main
python -m src.rq2_readability.main_analysis
```


### On local 
You can run the following commands on your local machine. But it takes a lot of time to process because of sequential execution
```
java --add-opens java.base/java.lang=ALL-UNNAMED -jar collect-refactoring-jar-with-dependencies.jar
java --add-opens java.base/java.lang=ALL-UNNAMED -jar collect-metrics-for-refactoring-jar-with-dependencies.jar
```
