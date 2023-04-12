package com.digdes.school;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class JavaSchoolStarter {
    public List<Map<String,Object>> data = new ArrayList<>();
    public String[] compareOperators = new String[] {"!=", "=", "ilike", "like", ">=", "<=", ">", "<"};
    public JavaSchoolStarter() {

    }
    public List<Map<String,Object>> execute(String request) throws Exception {
        //Здесь начало исполнения вашего кода
        String command = new String();
        String condition = new String();
        String logicalOperator = new String();

        int indexOfWhere = request.toUpperCase().indexOf("WHERE");
        if (indexOfWhere == -1) {
            command = request;
        }
        else {
            command = request.substring(0,indexOfWhere);
            // На случай пустого условия после "where"
            try {
                condition = request.substring(indexOfWhere + 6);
                if (condition.toUpperCase().indexOf(" AND ") != -1) {
                    logicalOperator = "AND";
                }
                else if (condition.toUpperCase().indexOf(" OR ") != -1) {
                    logicalOperator = "OR";
                }
            }
            catch (Exception ex) {
                condition = "";
            }
        }

        String[] commandParts = command.split(" ");
        switch (commandParts[0].toUpperCase()) {
            case "INSERT" -> {
                if (commandParts[1].toUpperCase().equals("VALUES")) {
                    List<Map<String,Object>> insertedData = new ArrayList<>();
                    Map<String,Object> row = new HashMap<>();
                    commandParts = processString(commandParts, 2);
                    putValues(commandParts, row);
                    data.add(row);
                    insertedData.add(row);
                    return insertedData;
                }
                else {
                    throw new Exception("invalid command");
                }
            }
            case "UPDATE" -> {
                if (commandParts[1].toUpperCase().equals("VALUES")) {
                    List<Map<String,Object>> updatedData = new ArrayList<>();
                    String[] conditionParts = processCondition(condition);
                    updatedData = update(data, conditionParts, commandParts, logicalOperator);
                    return updatedData;
                }
            }
            case "DELETE" -> {
                String[] conditionParts = processCondition(condition);
                List<Map<String,Object>> deletedData = new ArrayList<>();
                for (int i = 0; i <= data.size(); i++) {
                    Map<String,Object> row = delete(data, conditionParts, logicalOperator);
                    if (data.contains(row)){
                        deletedData.add(row);
                        data.remove(row);
                    }
                }
                return deletedData;
            }
            case "SELECT" -> {
                String[] conditionParts = processCondition(condition);
                List<Map<String,Object>> selectedData = new ArrayList<>();
                List<Map<String,Object>> copyData = new ArrayList<>();
                for (int i = 0; i < data.size(); i++) {
                    copyData.add(data.get(i));
                    // используем метод delete, т.к. в данном случае он испольняет ту же функцию, что и select
                    Map<String,Object> row = delete(copyData, conditionParts, logicalOperator);
                    if (copyData.contains(row)){
                        selectedData.add(row);
                        copyData.remove(row);
                    }
                }
                return selectedData;
            }
            default -> {
                throw new Exception("invalid original command");
            }
        }
        return data;
    }
    private String[] processString(String[] commandParts, int indexOfFirstValue) {
        String command = String.join("", Arrays.copyOfRange(commandParts, indexOfFirstValue, commandParts.length));
        command = command.replaceAll(" ", "");
        commandParts = command.split(",");
        return commandParts;
    }
    private String[] processCondition(String condition) {
        String[] conditionParts;
        // Нелья убирать пробелы до проверки наличия " AND ", т.к. есть возможность ошибиться, например "'lastName' = Andreev"
        int indexOfAnd = condition.toUpperCase().indexOf(" AND ");
        if (indexOfAnd != -1) {
            StringBuilder sb = new StringBuilder(condition);
            sb.replace(indexOfAnd,indexOfAnd + 5," AND ");
            condition = sb.toString();
            condition = condition.replaceAll(" ", "");
            conditionParts = condition.split("AND");
        }
        else if (condition.toUpperCase().contains(" OR ")) {
            int indexOfOr = condition.toUpperCase().indexOf(" OR ");
            StringBuilder sb = new StringBuilder(condition);
            sb.replace(indexOfOr,indexOfOr + 4," OR ");
            condition = sb.toString();
            condition = condition.replaceAll(" ", "");
            conditionParts = condition.split("OR");
        }
        else {
            condition = condition.replaceAll(" ", "");
            conditionParts = new String[]{condition};
        }
        return conditionParts;
    }
    private List<Map<String, Object>> update(List<Map<String,Object>> data, String[] conditionParts, String[] commandParts, String logicalOperator) throws Exception {
        int counter;
        List<Map<String,Object>> updatedData = new ArrayList<>();
        for (Map<String, Object> row : data) {
            if (conditionParts[0].isEmpty()) {
                // insert values
                commandParts = processString(commandParts, 2);
                putValues(commandParts, row);
                updatedData.add(row);
                continue;
            }
            counter = 0;
            for (String element : conditionParts) {
                String symbol = "";
                // Определяем тип оператора сравнения
                for (String operator: compareOperators) {
                    if (element.contains(operator)) {
                        symbol = operator;
                        break;
                    }
                }
                switch (symbol) {
                    case "=" -> {
                        // Данную строку нельзя поместить в метод 'operatorAction', т.к. значения 'elementParts' необходимы для булевого выражения
                        String[] elementParts = element.split(symbol);
                        // Инкементируем счетчик при совпадении условия
                        counter = operatorAction(counter, symbol, row.get(elementParts[0]).toString().equals(elementParts[1]));
                    }
                    case "!=" -> {
                        String[] elementParts = element.split(symbol);
                        counter = operatorAction(counter, symbol, !row.get(elementParts[0]).toString().equals(elementParts[1]));
                    }
                    case ">" -> {
                        String[] elementParts = element.split(symbol);
                        counter = operatorAction(counter, symbol, Double.parseDouble(row.get(elementParts[0]).toString()) > Double.parseDouble(elementParts[1]));
                    }
                    case "<" -> {
                        String[] elementParts = element.split(symbol);
                        counter = operatorAction(counter, symbol, Double.parseDouble(row.get(elementParts[0]).toString()) < Double.parseDouble(elementParts[1]));
                    }
                    case ">=" -> {
                        String[] elementParts = element.split(symbol);
                        counter = operatorAction(counter, symbol, Double.parseDouble(row.get(elementParts[0]).toString()) >= Double.parseDouble(elementParts[1]));
                    }
                    case "<=" -> {
                        String[] elementParts = element.split(symbol);
                        counter = operatorAction(counter, symbol, Double.parseDouble(row.get(elementParts[0]).toString()) <= Double.parseDouble(elementParts[1]));
                    }
                    case "like" -> {
                        String[] elementParts = element.split(symbol);
                        if (!elementParts[0].equals("'lastName'")) {
                            throw new Exception("invalid column name");
                        }
                        else{
                            elementParts[1] = elementParts[1].replace("%", "(.*)");
                            counter = operatorAction(counter, symbol, row.get(elementParts[0]).toString().matches(elementParts[1]));
                        }
                    }
                    case "ilike" -> {
                        String[] elementParts = element.split(symbol);
                        if (!elementParts[0].equals("'lastName'")) {
                            throw new Exception("invalid column name");
                        }
                        else{
                            elementParts[1] = elementParts[1].replace("%", "(.*)");
                            counter = operatorAction(counter, symbol, row.get(elementParts[0]).toString().toUpperCase().matches(elementParts[1].toUpperCase()));
                        }
                    }
                    default -> {
                        throw new Exception("invalid comparison operator");
                    }
                }
            }
            // Если количество выполненных условий совпадает с количеством условий в общем
            if ((logicalOperator.equals("AND") || logicalOperator.equals("")) && conditionParts.length == counter) {
                // insert values
                commandParts = processString(commandParts, 2);
                putValues(commandParts, row);
                updatedData.add(row);
            }
            else if (logicalOperator.equals("OR") && counter > 0){
                commandParts = processString(commandParts, 2);
                putValues(commandParts, row);
                updatedData.add(row);
            }
        }
        return updatedData;
    }
    // Копия update, кроме процесса изменения строки, т.к. нельзя вернуть в методе два объекта, возвращаем строку и проверяем имеется ли такая в таблице
    private Map<String,Object> delete(List<Map<String,Object>> data, String[] conditionParts,String logicalOperator) throws Exception {
        int counter;
        for (Map<String, Object> row : data) {
            if (conditionParts[0].isEmpty()) {
                // insert values
                return row;
            }
            counter = 0;
            for (String element : conditionParts) {
                String symbol = "";
                // Определяем тип оператора сравнения
                for (String operator: compareOperators) {
                    if (element.contains(operator)) {
                        symbol = operator;
                        break;
                    }
                }
                switch (symbol) {
                    case "=" -> {
                        // Данную строку нельзя поместить в метод 'operatorAction', т.к. значения 'elementParts' необходимы для булевого выражения
                        String[] elementParts = element.split(symbol);
                        // Инкементируем счетчик при совпадении условия
                        counter = operatorAction(counter, symbol, row.get(elementParts[0]).toString().equals(elementParts[1]));
                    }
                    case "!=" -> {
                        String[] elementParts = element.split(symbol);
                        counter = operatorAction(counter, symbol, !row.get(elementParts[0]).toString().equals(elementParts[1]));
                    }
                    case ">" -> {
                        String[] elementParts = element.split(symbol);
                        counter = operatorAction(counter, symbol, Double.parseDouble(row.get(elementParts[0]).toString()) > Double.parseDouble(elementParts[1]));
                    }
                    case "<" -> {
                        String[] elementParts = element.split(symbol);
                        counter = operatorAction(counter, symbol, Double.parseDouble(row.get(elementParts[0]).toString()) < Double.parseDouble(elementParts[1]));
                    }
                    case ">=" -> {
                        String[] elementParts = element.split(symbol);
                        counter = operatorAction(counter, symbol, Double.parseDouble(row.get(elementParts[0]).toString()) >= Double.parseDouble(elementParts[1]));
                    }
                    case "<=" -> {
                        String[] elementParts = element.split(symbol);
                        counter = operatorAction(counter, symbol, Double.parseDouble(row.get(elementParts[0]).toString()) <= Double.parseDouble(elementParts[1]));
                    }
                    case "like" -> {
                        String[] elementParts = element.split(symbol);
                        if (!elementParts[0].equals("'lastName'")) {
                            throw new Exception("invalid column name");
                        }
                        else{
                            elementParts[1] = elementParts[1].replace("%", "(.*)");
                            counter = operatorAction(counter, symbol, row.get(elementParts[0]).toString().matches(elementParts[1]));
                        }
                    }
                    case "ilike" -> {
                        String[] elementParts = element.split(symbol);
                        if (!elementParts[0].equals("'lastName'")) {
                            throw new Exception("invalid column name");
                        }
                        else{
                            elementParts[1] = elementParts[1].replace("%", "(.*)");
                            counter = operatorAction(counter, symbol, row.get(elementParts[0]).toString().toUpperCase().matches(elementParts[1].toUpperCase()));
                        }
                    }
                    default -> {
                        throw new Exception("invalid comparison operator");
                    }
                }
            }
            // Если количество выполненных условий совпадает с количеством условий в общем
            if ((logicalOperator.equals("AND") || logicalOperator.equals("")) && conditionParts.length == counter) {
                return row;
            }
            else if (logicalOperator.equals("OR") && counter > 0){
                return row;
            }
        }
        Map<String,Object> bolvanka = new HashMap<String,Object>();
        bolvanka.put("not a column", "not a value");
        return bolvanka;
    }
    private Integer operatorAction(int counter, String symbol, boolean condition) {
        try {
            if (condition) {
                counter++;
            }
        }
        catch (Exception ex) {
            System.out.format("invalid command for '%s'", symbol);
        }
        return counter;
    }
    private Map<String,Object> putValues(String[] commandParts, Map<String,Object> row) throws Exception {
        for (String element : commandParts) {
            String[] elementParts = element.split("=");
            switch (elementParts[0]) {
                case "'id'" -> {
                    row.put(elementParts[0], Long.parseLong(elementParts[1]));
                }
                case "'lastName'" -> {
                    row.put(elementParts[0], elementParts[1]);
                }
                case "'cost'" -> {
                    row.put(elementParts[0], Double.parseDouble(elementParts[1]));
                }
                case "'age'" -> {
                    row.put(elementParts[0], Long.parseLong(elementParts[1]));
                }
                case "'active'" -> {
                    row.put(elementParts[0], Boolean.parseBoolean(elementParts[1]));
                }
                default -> {
                    throw new Exception("invalid column name");
                }
            }
        }
        return row;
    }
}
