FROM ubuntu:latest

RUN apt-get update && apt-get install -y curl jq

COPY ./scripts/*.sh /usr/local/bin/
RUN chmod +x /usr/local/bin/*.sh

CMD ["sh", "-c", "$OPERASJON"]
