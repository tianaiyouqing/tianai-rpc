package cloud.tianai.rpc.core.context;

import cloud.tianai.rpc.common.URL;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: 天爱有情
 * @Date: 2020/03/03 12:10
 * @Description: RPC 上下文
 */
public class RpcContext {
    private static final ThreadLocal<RpcContext> LOCAL = ThreadLocal.withInitial(RpcContext::new);

    public static RpcContext getRpcContext() {
        return LOCAL.get();
    }

    public static void removeContext() {
        LOCAL.remove();
    }

    private final Map<String, Object> attachments = new HashMap<String, Object>();

    private Object request;
    private Object response;
    private URL remotingUrl;

    public void clearAttachments() {
        this.attachments.clear();
    }

    public RpcContext setAttachments(Map<String, Object> attachment) {
        this.attachments.clear();
        if (attachment != null && attachment.size() > 0) {
            this.attachments.putAll(attachment);
        }
        return this;
    }

    public Map<String, Object> getAttachments() {
        return attachments;
    }


    public RpcContext removeAttachment(String key) {
        attachments.remove(key);
        return this;
    }


    public RpcContext setAttachment(String key, Object value) {
        if (value == null) {
            attachments.remove(key);
        } else {
            attachments.put(key, value);
        }
        return this;
    }

    public Object getAttachment(String key) {
        return attachments.get(key);
    }


    public RpcContext setRequest(Object request) {
        this.request = request;
        return this;
    }


    public RpcContext setResponse(Object response) {
        this.response = response;
        return this;
    }

    public RpcContext setRemotingUrl(URL remotingUrl) {
        this.remotingUrl = remotingUrl;
        return this;
    }

    public URL getRemotingUrl() {
        return remotingUrl;
    }

    public Object getRequest() {
        return this.request;
    }

    public Object getResponse() {
        return this.response;
    }
}
