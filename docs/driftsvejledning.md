Driftsvejledning
================

JBoss konfiguration
-------------------
JBoss har en række system-properties som kan være relevante at konfigurere. Se dk.os2opgavefordeler.service.ConfigService for oplysninger om hvilke der findes.

JBoss logging
-------------
JBoss logger fra backend applikationen til server.log

Kommunespecifik sql backup
--------------------------
For at give en kommune mulighed for at lave kommunespecifik backup af sql i et hosted setup, er der udviklet et sql script som via crontab bygger et script 
som kan etablere db som den var da scriptet blev eksekveret. Se software-guidebook.md for mere info.

