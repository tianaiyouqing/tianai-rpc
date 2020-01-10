package cloud.tianai.rpc.demo.api;

public interface DemoService {

    DemoResult req(String str, Integer id, DemoRequest request);
}
