import os
from langchain_community.document_loaders import DirectoryLoader, UnstructuredMarkdownLoader
from langchain_text_splitters import RecursiveCharacterTextSplitter
from langchain_ollama import OllamaEmbeddings
from langchain_qdrant import QdrantVectorStore
from qdrant_client import QdrantClient
from qdrant_client.http import models

BASE_DIR = os.path.dirname(os.path.abspath(__file__))

def importar_capitulos():
    path_pasta = os.path.join(BASE_DIR, 'livro_dividido')
    url_qdrant = "http://localhost:6333"
    nome_colecao = "meu_livro_ollama"

    print(f"--- Buscando em: {path_pasta} ---")

    try:
        # 1. Carrega e divide os arquivos
        loader = DirectoryLoader(path_pasta, glob="**/*.md", loader_cls=UnstructuredMarkdownLoader)
        docs = loader.load()
        
        if not docs:
            print(f"ERRO: Nenhum arquivo encontrado em {path_pasta}")
            return

        text_splitter = RecursiveCharacterTextSplitter(chunk_size=1000, chunk_overlap=200)
        chunks = text_splitter.split_documents(docs)
        print(f"Arquivos: {len(docs)} | Fragmentos: {len(chunks)}")

        # 2. Inicializa o Embedding do Ollama
        embeddings_model = OllamaEmbeddings(model="nomic-embed-text")

        # 3. Conecta ao Cliente Qdrant
        client = QdrantClient(url=url_qdrant)

        # 4. Criar Coleção (Substituindo o recreate_collection obsoleto)
        print(f"Configurando coleção '{nome_colecao}'...")
        if client.collection_exists(nome_colecao):
            client.delete_collection(nome_colecao)
            
        client.create_collection(
            collection_name=nome_colecao,
            vectors_config=models.VectorParams(size=768, distance=models.Distance.COSINE),
        )

        # 5. Envia os documentos (Corrigido para 'embedding' no singular)
        print("Enviando documentos para o Qdrant... Isso pode levar um minuto.")
        vector_store = QdrantVectorStore(
            client=client,
            collection_name=nome_colecao,
            embedding=embeddings_model, # O parâmetro correto é sem o 's' final
        )
        
        vector_store.add_documents(chunks)

        print("--- SUCESSO ABSOLUTO! ---")
        print(f"Confira aqui: http://localhost:6333/dashboard")

    except Exception as e:
        print(f"OCORREU UM ERRO: {e}")

if __name__ == "__main__":
    importar_capitulos()