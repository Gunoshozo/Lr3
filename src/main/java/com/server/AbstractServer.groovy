package com.server

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.junit.jupiter.engine.discovery.predicates.IsInnerClass

abstract class AbstractServer {


    protected final Integer WIN_NUMBER = 42

    protected boolean Even_turn = false

    protected volatile Integer MagicNumber = 1
    protected List<ClientHandler> Users = new ArrayList<>()
    protected def serverSocket = new ServerSocket(6666)
    protected volatile List<String> Logs = new ArrayList<>()

    static AbstractServer init(boolean flag){
        if(flag){
            return new DecimalServer()
        }
        else{
            return new FullServer()
        }
    }


    void start(){
        InetAddress inetAddress = InetAddress.getLocalHost()
        println inetAddress.getHostAddress()
        while (Users.size() < 2){
            Socket s = serverSocket.accept()
            Users.add(new ClientHandler(s))
            addLog("Client ${Users.last().clientSocket.getInetAddress().toString()[1..-1]} connected")
            println getLastLog()
            new Thread(Users.last()).start()
        }
        Users[0].setPlayerEven(new Random().nextBoolean())
        Users[1].setPlayerEven(!Users[0].isPlayerEven())
        for(int i=0;i < Users.size();i++){
            Users[i].setReady(true)
        }

    }

    protected def calculate(arg){
        switch (arg.first){
            case '+':
                MagicNumber+=arg.second
                break
            case '*':
                MagicNumber*=arg.second
                break
            case '-':
                MagicNumber-=arg.second
                break
            case '/':
                MagicNumber/=arg.second
                break
            case '%':
                MagicNumber%=arg.second
                break
            case '^':
                MagicNumber**=arg.second
                break

        }
        return MagicNumber
    }

    protected boolean isWin(){
        return MagicNumber == WIN_NUMBER
    }

    protected List<Boolean> HealthCheck(){
        def isOk = new ArrayList<Boolean>()
        Users.each {
            isOk.add(!it.clientSocket.isClosed())
        }
        return isOk
    }

    protected void sendAll(content){
        for(def u:Users){
            return
        }

    }

    protected void StopAll(){
        for (int i = 0; i < Users.size(); i++) {
            if(!Users.last().clientSocket.isClosed())
                Users.last().clientSocket.close()
            Users.last().Stop()
            Users.remove(Users.size()-1)
        }
    }

    protected void addLog(String s){
        Logs.add(s)
    }

    List<String> getLogs() {
        return Logs
    }

    String getLastLog(){
        return Logs.last()
    }

    protected void changeTurn(){
        Even_turn = !Even_turn
    }

//Генерация чисел
    abstract List<Tuple2<String,Integer>> GenerateInputs()

    class ClientHandler implements Runnable {
        private Socket clientSocket
        private boolean EvenPlayer = false
        private def Ops
        private boolean Ready = false
        private boolean ShouldRun

        ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket
        }

        void setReady(boolean ready) {
            Ready = ready
        }

        Boolean isPlayerEven() {
            return EvenPlayer
        }

        void setPlayerEven(Boolean playerOrder) {
            this.EvenPlayer = playerOrder
        }

        void Stop(){
            this.ShouldRun = false
        }

        @Override
        void run() {
            ShouldRun = true
            while(true)
            {
                if (clientSocket.isClosed()){
                    Users.remove(this)
                    return
                }
                if (this.Ready)
                    break

            }
            this.Ops = GenerateInputs()
            clientSocket.withStreams {input,output->
                sendInit(output)
                while (ShouldRun){
                    if(clientSocket.isClosed()){
                        println "${EvenPlayer?'Second':'First'} player was disconected"
                        StopAll()
                        return
                    }
                    def JsonString = input.newObjectInputStream().readObject()
                    JsonSlurper Slurper = new JsonSlurper()
                    def Response = Slurper.parseText(JsonString)
                    handleResponse(Response)
                }
            }
        }

        /*ToDo Написать обработчик ответа
          Придумать ответы */
        void handleResponse(Response) {
            switch(Response["type"]){
                case "": break
            }
        }

        void sendInit(OutputStream output){
            def data = [type:'Init',
                    even:EvenPlayer,
                    ops:Ops,
                    val:MagicNumber
            ]
            def Json = JsonOutput.toJson(data)
            output.newObjectOutputStream().writeObject(Json)
        }
    }
}
