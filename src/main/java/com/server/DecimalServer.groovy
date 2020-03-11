package com.server

class DecimalServer extends AbstractServer{

    @Override
    List<Tuple2<String,Integer>> GenerateInputs() {
        ArrayList<String> Ops = ["*","+","-","/","^","%"]
        def Res = new ArrayList<Tuple2<String,Integer>>()
        Random r = new Random()
        Ops.remove(r.nextInt(Ops.size()))
        Ops.each {it->
            switch (it){
                case '*':
                    Res.add(new Tuple2(it,r.nextInt(19)-9))
                    break
                case '^':
                case '+':
                case '-':
                    Res.add(new Tuple2(it,r.nextInt(10)))
                    break
                case '/':
                    Integer num = r.nextInt(19)-9
                    while (num == 0)
                        num = r.nextInt(19)-9
                    Res.add(new Tuple2(it,num))
                    break
                case '%':
                    Res.add(new Tuple2(it,r.nextInt(9)+1))
                    break
            }
        }
        return Res
    }
}
