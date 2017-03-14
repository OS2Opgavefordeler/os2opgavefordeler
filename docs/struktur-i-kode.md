Strukturering af koden
======================

Koden er organiseret på følgende måde

Backend ( fra src/main/java
---------------------------
**dk.os2opgavefordeler.assigneesearch** - relateret til tildelinger.
**dk.os2opgavefordeler.auth** - alt omkring authentication og authorization inkl. auth via openid og OS2SSO.
**dk.os2opgavefordeler.distribution** relateret til udvidede fordelinger.
**dk.os2opgavefordeler.kle** Repository for KLE.
**dk.os2opgavefordeler.logging** Kode for logging og auditlogning.
**dk.os2opgavefordeler.migration** Annotation og Extension for eager load at beans.
**dk.os2opgavefordeler.model** Entity Beans, Data Transfer Objects og Presentation Objects (PO).
**dk.os2opgavefordeler.orgunit** Kode til at håndtere import af organisation.
**dk.os2opgavefordeler.repository** Repositories for de forskellige entiteter.
**dk.os2opgavefordeler.rest** REST endpoints, inkl. et for eksterne kald, api, og en superklasse til fælles funktionalitet.
BEMÆRK forskellig authentication for api og ikke-api endpoints.
**dk.os2opgavefordeler.service** Services for applikationen.
**dk.os2opgavefordeler.util** util klasser bl.a. til validering og udvidede regler (FilterHelper)

Frontend (fra src/main/webapp)
------------------------------
**root** - build files og diverse dependency management
**app** - index.html og app.js
**app.common** - modaler.
**app.home** - filer til billedet for fordeling.
**app.layout** - login og layout filer.
**app.municipality-admin** - filer til kommuneadmin funktionalitet.
**app.services** - services, herunder til kommunikation med backend.
**app.settings** - sysadm features.
**assets** - billeder og eksempel json.
**config** - konfigfil.
**devServer** - forberedt for stubs.
**lib** - dependencies installeres her.
**styles** - less og kompileret stylesheet.
