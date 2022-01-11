package functions;

public class Response<T> {
    private AuditType type;
    private String userId;
    private T data;

    public Response() {}

    public Response(AuditType shots, String userId, T data) {
        this.type = shots;
        this.userId = userId;
        this.data = data;
    }

    public AuditType getType() {
        return type;
    }

    public String getUserId() {
        return userId;
    }

    public T getData () {
        return data;
    }
}
