# ==========================================
# ÉTAPE 1 : Compilation (Build Stage)
# ==========================================
# On utilise une image officielle contenant Maven et Java 21 pour compiler
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build

# Définition du répertoire de travail dans le conteneur
WORKDIR /app

# On copie d'abord le fichier de configuration Maven pour mettre en cache les dépendances
COPY pom.xml .

# On télécharge les dépendances (évite de tout retélécharger si le pom ne change pas)
RUN mvn dependency:go-offline -B

# On copie tout le reste du code source du projet
COPY src ./src

# On compile le projet et on génère le fichier .jar (en passant les tests pour accélérer le build de prod)
RUN mvn package -DskipTests

# ==========================================
# ÉTAPE 2 : Exécution (Runtime Stage)
# ==========================================
# On utilise une image Java ultra-légère (Alpine Linux) juste pour exécuter l'API
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# On récupère le fichier .jar généré à l'étape précédente
COPY --from=build /app/target/*.jar app.jar

# On expose le port sur lequel Spring Boot écoute par défaut
EXPOSE 8080

# Commande pour démarrer l'API au lancement du conteneur
ENTRYPOINT ["java", "-jar", "app.jar"]