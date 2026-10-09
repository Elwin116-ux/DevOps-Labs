FROM mysql:8.0

# Set the working directory
WORKDIR /tmp

# Copy all the files to the working directory of the container
COPY test_db/*.sql /tmp/
COPY test_db/*.dump /tmp/

# Copy the main SQL file to docker-entrypoint-initdb.d
# Scripts and SQL files in this folder are executed on container startup
COPY test_db/employees.sql /docker-entrypoint-initdb.d

# Set the root password
ENV MYSQL_ROOT_PASSWORD=example
ENV MYSQL_ROOT_HOST=%