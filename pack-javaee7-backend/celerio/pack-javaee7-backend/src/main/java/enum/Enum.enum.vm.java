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
$output.java($enum.model.packageName, "$enum.model.type")##

$output.require($EnumModelSupport, "LabelizedEnum")##
$output.require($Util, "ResourcesUtil")##

$enum.config.getJavadoc()##
public enum $enum.model.type implements LabelizedEnum {
#if (!$enum.config.isCustomType())
#foreach ($enumValue in $enum.config.enumValues)
    ${enumValue.name}$project.print($velocityHasNext, ", //", ";")
#end
#else
#foreach ($enumValue in $enum.config.enumValues)
    ${enumValue.name}("$enumValue.value")$project.print($velocityHasNext, ", //", ";")
#end

    private final String value;

    /**
     * @param value The value that is persisted in the database.
     */
    ${enum.model.type}(String value) {
        this.value = value;
    }

    /**
     * @return the value that is persisted in the database.
     */
    public String getValue() {
        return value;
    }

    /**
     * @return the $enum.model.type instance having its value matching exactly the passed value. 
     */
    public static $enum.model.type fromValue(String value) {
        if (value == null) {
            return null;
        }
        for ($enum.model.type e : ${enum.model.type}.values()) {
            if (value.equals(e.getValue())) {
                return e;
            }
        }
        return null;
    }
#end

    @Override
    public String getLabel() {
        return ResourcesUtil.getInstance().getProperty("${enum.model.type}_" + name());
    }

    public static $enum.model.type fromLabel(String label) {
        if (label == null) {
            return null;
        }
        for ($enum.model.type enumValue : ${enum.model.type}.values()) {
            if (label.equals(enumValue.getLabel())) {
                return enumValue;
            }
        }
        return null;
    }
}