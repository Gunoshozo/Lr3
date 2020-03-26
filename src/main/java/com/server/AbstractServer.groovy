package com.server

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import com.UI.UIForm

abstract class AbstractServer extends Thread{


    protected final Integer WIN_NUMBER = 42

    protected boolean EvenTurn = false

    protected volatile Integer MagicNumber = 1
    protected List<ClientHandler> Users = new ArrayList<>()
    protected def serverSocket = new ServerSocket(6666)
    protected volatile List<String> Logs = new ArrayList<>()
    protected UIForm form


    void setUIForm(UIForm) {
        this.form = UIForm
    }

    UIForm getForm() {
        return form
    }

    static AbstractServer init(boolean flag){
        if(flag){
            return new DecimalServer()
        }
        else{
            return new FullServer()
        }
    }


    void run(){
        InetAddress inetAddress = InetAddress.getLocalHost()
        form.setLogs("Server started at "+inetAddress.getHostAddress().toString())
        while (Users.size() < 2){
            Socket s = serverSocket.accept()
            Users.add(new ClientHandler(s))

            addLog("Client ${Users.last().clientSocket.getInetAddress().toString()[1..-1]} connected")
            form.appendLogs(getLastLog())

            form.updatePlayers(Users.size())

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

        Users.each {
            it->
                it.outputStream << content
        }

    }

    protected void StopAll(){
        for (int i = 0; i < Users.size(); i++) {
            if(!Users.last().clientSocket.isClosed())
                Users.last().clientSocket.close()
            Users.last().Stop()
            Users.remove(Users.size()-1)
        }
        form.updatePlayers(Users.size())
    }

    protected void StopAll1(){
        for (int i = 0; i < Users.size(); i++){
            Users[i].ShouldRun = false
        }
    }

    protected void removeUser(obj){
        Users.remove(obj)
    }

    protected void addLog(String s){
        Logs.add(s)
    }

    protected void updateLogs(){
        form.setLogs(Logs)
    }

    List<String> getLogs() {
        return Logs
    }

    String getLastLog(){
        return Logs.last()
    }

    protected void changeTurn(){
        EvenTurn = !EvenTurn
    }


    abstract List<Tuple2<String,Integer>> GenerateInputs()

    class ClientHandler implements Runnable {
        private Socket clientSocket
        private boolean EvenPlayer = false
        private def Ops
        private boolean Ready = false
        private boolean ShouldRun
        private def outputStream

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
                    form.updatePlayers(Users.size())
                    return
                }
                if (this.Ready)
                    break

            }
            this.Ops = GenerateInputs()
            clientSocket.withStreams {input,output->
                outputStream = output
                sendInit(output)
                while (ShouldRun){
                    if(clientSocket.isClosed()){
                        println "${EvenPlayer?'Second':'First'} player was disconected"
                        StopAll()
                        return
                    }
                    def JsonString = input.newReader().readLine()
                    JsonSlurper Slurper = new JsonSlurper()
                    def Response = Slurper.parseText(JsonString)
                    handleResponse(Response)
                }
            }
            removeUser(this)
        }

        /*ToDo Написать обработчик ответа
          Придумать ответы */
        void handleResponse(Response) {
            switch(Response["type"]){
                case "move":
                    proccesOp(Response["oper"])
            }
        }

        void sendInit(OutputStream output){
            def data = [type:'Init',
                    even:EvenPlayer,
                    ops:Ops,
                    val:MagicNumber,
                    turn:false
                    ]
            def Json = JsonOutput.toJson(data) + '\n'
            output << Json
        }

        void proccesOp(int op){
           if(this.EvenPlayer == EvenTurn){
               switch (Ops[op].getFirst()){
                   case '*':
                       MagicNumber *= Ops[op].getSecond()
                       break
                   case '/':
                       MagicNumber = (int)(MagicNumber/ Ops[op].getSecond())
                       break
                   case '-':
                       MagicNumber -= Ops[op].getSecond()
                       break
                   case '+':
                       MagicNumber += Ops[op].getSecond()
                       break
                   case '^':
                       MagicNumber**=Ops[op].getSecond()
                      break
                   case '%':
                       MagicNumber%= Ops[op].getSecond()
                       break
               }
               form.UpdateNumber(MagicNumber)
               def logStr = "User ${(Users[0] == this)?"№1":"№2"} changed magic number to ${MagicNumber}"
               addLog(logStr)
               form.appendLogs(getLastLog())
           }
            if (MagicNumber == WIN_NUMBER){
                endGame()
            }
            else {
                changeTurn()
                def body = [
                        "type": "NewTurn",
                        "turn": EvenTurn,
                        "val" : MagicNumber,
                ]
                def Json = JsonOutput.toJson(body) + '\n'
                nextTurn(Json)
            }
        }

        //todo
        void endGame(){
            def body = [
                    "type": "End",
                    "turn": EvenTurn,
                    "val": MagicNumber
            ]
            def Json = JsonOutput.toJson(body) + '\n'
            sendAll(Json)
            ShouldRun = false
            StopAll1()
            def logStr = "User ${(Users[0] == this)?"№1":"№2"} Won"
            addLog(logStr)
            form.appendLogs(getLastLog())
        }

        void nextTurn(body){
            sendAll(body)
        }
    }
}
