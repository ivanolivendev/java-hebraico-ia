# Dockerfile - usando imagem oficial do Qdrant
FROM qdrant/qdrant:latest

# Volumes opcionais para persistência de dados e configuração
VOLUME /qdrant/storage
VOLUME /qdrant/config

# Expõe portas REST e gRPC
EXPOSE 6333 6334

# NÃO precisa sobrescrever CMD; a imagem oficial já inicia o Qdrant corretamente