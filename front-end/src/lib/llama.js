export async function askAI(question, pdfContext = "") {
  const systemMessage = pdfContext
    ? `Você é um assistente de leitura. Responda perguntas baseado no seguinte conteúdo do PDF:\n\n${pdfContext}\n\nSe a pergunta não estiver relacionada ao conteúdo do PDF, avise o usuário. Você pode utilizar seus conhecimentos gerais caso necessário, tenha em mente que você receberá conteúdo com base num range de páginas específicos. Mas só utilize esse conhecimento prévio caso haja informações insuficientes no conteúdo atual.`
    : "Você é um assistente inteligente. Responda perguntas de forma clara e concisa.";

  console.log("Conteúdo do pdf: ", pdfContext);
  const response = await fetch("http://127.0.0.1:11434/api/chat", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      model: "gemma3:4b",
      stream: false,
      messages: [
        { role: "system", content: systemMessage },
        { role: "user", content: question }
      ]
    })
  })

  const data = await response.json()
  return data.message.content
}