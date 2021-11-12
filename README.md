# LoggerV2
Modelo de leitura dos dados dos sensores com SensorEventListener atuando em background com Services e Threads.

Sobre a lógica implementada: Uma segunda thread foi criada para mostrar os dados — que posteriormente serão dispostos em um arquivo à parte — para que fosse possivel utilizar a função Thread.sleep, para atuar como um timer — por se tratar de um delay que congela a tarefa, ao colocar na thread principal todo o sistema é travado por conta dele — já que o escopo do projeto no presente momento requer que os dados sejam armazenados em um espaço de tempo definido. Portanto, a leitura dos sensores é feita na thread principal e na secundária é feita a manipulação desse dado. Vale ressaltar que provavelmente esta não seja a mais adequada e eficiente escolha, porém, como estou conhecendo e aprendendo Java agora, foi a alternativa que estava ao meu alcance e que qualquer sugestão é bem-vinda para otimizar a aplicação.
