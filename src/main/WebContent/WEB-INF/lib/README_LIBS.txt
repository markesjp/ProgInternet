Coloque aqui (arraste para dentro desta pasta) os JARs das bibliotecas:

1) commons-fileupload2-2.0.0-M4 (use os JARs do módulo *javax* se seu projeto está em javax.servlet):
   - commons-fileupload2-core-2.0.0-M4.jar
   - commons-fileupload2-javax-2.0.0-M4.jar

2) commons-io-2.21.0:
   - commons-io-2.21.0.jar

Observações importantes:
- O Commons FileUpload 2.0.0-M4 requer Java 11 (ou superior).
- Em projetos Dynamic Web, qualquer JAR em WEB-INF/lib já entra no classpath do WAR.
- Após copiar os JARs, faça um Clean/Build no Eclipse.
