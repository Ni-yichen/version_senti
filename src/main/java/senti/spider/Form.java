package senti.spider;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Form {

    @CsvBindByPosition(position = 0)
    public  int id;
    @CsvBindByPosition(position = 1)
    public  String name;
    @CsvBindByPosition(position = 2)
    public  String version;
    @CsvBindByPosition(position = 3)
    public  String date;
    @CsvBindByPosition(position = 4)
    public String text;
    @CsvBindByPosition(position = 5)
    public String role;
    @CsvBindByPosition(position = 6)
    public String topic;
    @CsvBindByPosition(position = 7)
    public String score;
    @CsvBindByPosition(position = 8)
    public String tag;
}
