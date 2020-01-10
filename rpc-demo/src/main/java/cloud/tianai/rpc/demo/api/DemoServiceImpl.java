package cloud.tianai.rpc.demo.api;

public class DemoServiceImpl implements DemoService {
    @Override
    public DemoResult req(String str, Integer id, DemoRequest request) {
        DemoResult result = new DemoResult();
        result.setStr(str);
        result.setId(id);
        result.setRequest(request);
        return result;
    }
}
