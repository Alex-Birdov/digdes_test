package com.digdes.school;

public class Main {
    public static void main(String[] args) {
        JavaSchoolStarter starter = new JavaSchoolStarter();
        try {
//            starter.execute("asdwq");
            System.out.println(starter.execute("insert VALUES 'lastName' = 'Федоров' , 'id'=1, 'age'=40, 'active'=false"));
            System.out.println(starter.execute("insert values 'lastName' = 'Иванов' , 'id'=2, 'age'=20, 'active'=false"));
            System.out.println(starter.execute("UPDATE VALUES 'active'=true where 'age' < 30"));
            System.out.println((starter.execute("select where 'id' > 0")));
            System.out.println((starter.execute("delete where 'id' = 1 ")));
            System.out.println(starter.execute("insert VALUES 'lastName' = 'Петров' , 'id'=3, 'age'=35, 'active'=true"));
            System.out.println((starter.execute("SELECT")));
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }
}