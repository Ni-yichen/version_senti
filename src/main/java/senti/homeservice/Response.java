package senti.homeservice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Response {
    private String code;
    private String msg;
    private String result;

    public Response(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static Response buildSuccess(String  result) {
        return new Response("00000", "Success", result);
    }

}
