Webscraper en Java 21 de datos de trabajos y empresas en diferentes portales (e.g. LinkedIn, MalditasConsultoras, Glassdoor) usando jsoup y Spring Batch (Spring Boot 4.0).

Se incluyen 2 Jobs de Spring Batch:
- CurroImporter: A partir de las URLs (LinkedIn) de trabajos en el CSV "data/curro-importer/input/curros.csv" extrae info de cada trabajo y su empresa correspondiente en diversos portales (e.g. MalditasConsultoras, Glassdoor) escribiendo los resultados en CSVs en "data/curro-importer/generated"
- CompanyImporter: A partir de los nombres de empresas en el CSV "data/company-importer/input/company-names.csv" extrae la info en "data/company-importer/generated/company-info.csv"
