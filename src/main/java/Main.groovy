import com.server.AbstractServer
import groovy.json.JsonOutput

class Main {
    static void main(String[] args) {
//        def form = new UIForm()
//        form.setVisible(true)
        AbstractServer abstractServer = AbstractServer.init(true)
        abstractServer.start()

    }
}
