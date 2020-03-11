package com.server

class FullServer extends AbstractServer{

    @Override
    List<Tuple2<String,Integer>> GenerateInputs() {
        ArrayList<String> Ops = ["*","+","-","/","^","%"]
        def Res = new ArrayList<Tuple2<String,Integer>>()
        Random r = new Random()
        Ops.remove(r.nextInt(Ops.size()))
        Ops.each{ it->
            switch (it){
                case '*':
                case '+':
                case '-':
                    Res.add(new Tuple2(it,r.nextInt()))
                    break
                case '^':
                    Integer num = r.nextInt()
                    while (num < 0)
                        num = r.nextInt()
                    Res.add(new Tuple2(it,num))
                    break
                case '/':
                    Integer num = r.nextInt()
                    while (num == 0)
                        num = r.nextInt()
                    Res.add(new Tuple2(it,num))
                    break
                case '%':
                    Integer num = r.nextInt()
                    while (num <= 0)
                        num = r.nextInt()
                    Res.add(new Tuple2(it,num))
                    break
            }
        }
        return Res
    }




}
