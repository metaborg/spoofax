{{pkg}}

public class {{id}} {{implementSorts}} {
{{#fields}}    public final {{type}} {{id}};
{{/fields}}

    public {{id}}({{fieldsCsv}}) {
    {{#fields}}    this.{{id}} = {{id}};
    {{/fields}}        
    }
}
