package it.polimi.tiw.courses;

public class Course {

    private Integer id;
    private String code;
    private String name;
    private String professor;
    private String description;
    private Integer hours;

    public Course(Integer id, String code, String name, String professor, String description, Integer hours) {
        super();
        this.id = id;
        this.code = code;
        this.name = name;
        this.professor = professor;
        this.description = description;
        this.hours = hours;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getHours() {
        return this.hours;
    }

    public void setHours(Integer hours) {
        this.hours = hours;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfessor() {
        return professor;
    }

    public void setProfessor(String professor) {
        this.professor = professor;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
