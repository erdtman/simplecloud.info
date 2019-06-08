

const source   = document.getElementById("implementations_template").innerHTML;
const template = Handlebars.compile(source);

const sorted_scim_v1_implementations = scim_v1_implementations.sort((a,b) => a.project_name > b.project_name);
const sorted_scim_v2_implementations = scim_v1_implementations.sort((a,b) => a.project_name > b.project_name);

const scim_v1_implementations_html = template(sorted_scim_v1_implementations);
const scim_v2_implementations_html = template(sorted_scim_v2_implementations);

document.getElementById("scim_v1_implementations").innerHTML = scim_v1_implementations_html;
document.getElementById("scim_v2_implementations").innerHTML = scim_v2_implementations_html;
