## Copyright 2015 JAXIO http://www.jaxio.com
##
## Licensed under the Apache License, Version 2.0 (the "License");
## you may not use this file except in compliance with the License.
## You may obtain a copy of the License at
##
##    http://www.apache.org/licenses/LICENSE-2.0
##
## Unless required by applicable law or agreed to in writing, software
## distributed under the License is distributed on an "AS IS" BASIS,
## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
## See the License for the specific language governing permissions and
## limitations under the License.
##
$output.java($WebFilter, "DelegatingFilter")##

$output.require("java.io.IOException")##
$output.require("javax.servlet.Filter")##
$output.require("javax.servlet.FilterChain")##
$output.require("javax.servlet.FilterConfig")##
$output.require("javax.servlet.ServletException")##
$output.require("javax.servlet.ServletRequest")##
$output.require("javax.servlet.ServletResponse")##
$output.require("org.apache.deltaspike.core.api.provider.BeanProvider")##

public class $output.currentClass implements Filter {
    private Filter innerFilter;
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String filterBeanName = filterConfig.getFilterName();
        innerFilter = BeanProvider.getContextualReference(filterBeanName, false, Filter.class);
        innerFilter.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        innerFilter.doFilter(request, response, chain);
    }

    @Override
    public void destroy() {
        innerFilter.destroy();
    }
}