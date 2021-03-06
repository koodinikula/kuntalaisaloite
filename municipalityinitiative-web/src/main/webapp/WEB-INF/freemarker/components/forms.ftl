<#import "/spring.ftl" as spring />
<#import "utils.ftl" as u />

<#escape x as x?html> 

<#--
 * showError
 *
 * Validation errors that are binded in specific field
 * 
 * @param cssClass for custom styling. Multiple classes are separated with a space
-->
<#macro showError cssClass="">
    <#if spring.status.error>
        <#list spring.status.errorMessages as error>
            <#noescape><div id="${spring.status.expression}-error" class="field-error ${cssClass}"><span class="icon-small icon-16 cancel"></span> <span class="message">${error!''}</span></div></#noescape>
        </#list>
    </#if>
</#macro>


<#macro uploadField path cssClass="" attributes="" maxLength="" cssErrorClass="" name="image" multiple=false>
    <@spring.bind path />
    <@showError cssClass=cssErrorClass />
    <input class="${cssClass}" type="file" name=${name} <#if multiple>multiple</#if>>
</#macro>


<#--
 * formLabel
 *
 * For-attribute needs to be fixed so that it would correspond with input's name (srping.status.expression).
 *  
 * @param path the name of the field to bind to
 * @param required generates an icon and can be used in JS-validation
 * @param optional additional information for label
 * @param key optional key for label message
-->
<#macro formLabel path required optional key="">
    <#if key == "">
        <#assign labelKey = fieldLabelKey(path) />
    <#else>
        <#assign labelKey = key />
    </#if>
    
    <#assign forAttr = spring.status.expression />
            
    <label class="input-header" for="${forAttr!""}">
        <@u.message labelKey /> <#if required != ""><@u.icon type="required" size="small" /></#if>
        <#if optional>
            <span class="instruction-text"><@u.message labelKey + ".optional" /></span>
        </#if>
    </label>

    <#nested/>
</#macro>

<#--
 * textField
 *
 * Textfield with label -option
 *
 * @param path the name of the field to bind to
 * @param required generates an icon and can be used in JS-validation
 * @param optional additional information for label
 * @param cssClass for custom styling. Multiple classes are separated with a space
 * @param attributes for example maxlength=\"7\"
 * @param fieldType text, date, email, ...
 * 
-->
<#macro textField path required optional cssClass="" attributes="" maxLength="" fieldType="text" key="">
    <@spring.bind path />  
    

    <@formLabel path required optional key>
        <@showError />
        <@spring.formInput path, 'class="'+cssClass+' '+spring.status.error?string("error","")+'" '+required+' maxlength="'+maxLength+'" '+attributes fieldType />
    </@formLabel>
</#macro>

<#--
 * simpleTextField
 *
 * Simple TextField without label -option
 *
 * @param path the name of the field to bind to
 * @param cssClass for custom styling. Multiple classes are separated with a space
 * @param attributes for example 'maxlength="7"'
 * @param cssErrorClass for customization of the error-message
 * 
-->
<#macro simpleTextField path cssClass="" attributes="" maxLength="" cssErrorClass="">
    <@spring.bind path />  
    
    <@showError cssClass=cssErrorClass />
    
    <@spring.formInput path, 'class="'+cssClass+' '+spring.status.error?string("error","")+'" '+required+' maxlength="'+maxLength+'" '+attributes />
</#macro>

<#--
 * textarea
 *
 * Note: The maxlength attribute of the <textarea> tag is not supported in Internet Explorer 9 and earlier versions, or in Opera.
 *
 * @param path the name of the field to bind to
 * @param required generates an icon and can be used in JS-validation
 * @param optional additional information for label
 * @param cssClass for custom styling. Multiple classes are separated with a space
 * @param key is optional custom key for label
 * @param maxLength HTML5 attribute for max length
-->
<#macro textarea path required optional cssClass="" key="" maxLength="">
    <@spring.bind path />  

    <@formLabel path required optional key>
        <@showError />
        <@spring.formTextarea path, 'class="'+cssClass+' '+spring.status.error?string("error","")+'" '+required+' maxlength="'+maxLength+'" ' />
    
    </@formLabel>
</#macro>

<#--
 * formCheckbox
 *
 * @param path the name of the field to bind to
 * @param attributes an additional string of arbitrary tags or text to be included within the HTML tag itself
 * @param prefix for custom messages
-->
<#macro formCheckbox path checked=false attributes="" key="">
    <@spring.bind path />
    <#assign id="${spring.status.expression}">
    <#if spring.status.value??>
        <#assign isSelected = spring.status.value?string=="true" />
    <#else>
        <#assign isSelected = checked />
    </#if>
    <input type="hidden" name="_${id}" value="on" />
    
    <@showError />
     
    <label class="inline">
        <input type="checkbox" id="${id}" name="${id}"<#if isSelected> checked="checked"</#if> ${attributes}/>
        <span class="label"><@u.message (key!="")?string(key,path) /></span>
    </label>
</#macro>


<#--
 * radiobutton
 *
 * @param path the name of the field to bind to
 * @param options a Map of all the available values that can be selected from in the input field.
 * @param required generates an icon and can be used in JS-validation
 * @param attributes an additional string of arbitrary tags or text to be included within the HTML tag itself
-->
<#macro radiobutton path options required="" attributes="" header=true  key="">
    <@spring.bind path />  
    
     <#if key == "">
        <#assign labelKey = fieldLabelKey(path) />
    <#else>
        <#assign labelKey = key />
    </#if>
 
    <#if header>
        <div class="input-header">
            <@u.message labelKey /><#if required != ""> <@u.icon type="required" size="small" /></#if>
        </div>
    </#if>
    
    <@showError />
    
    <#list options?keys as value>
        <label>
            <input type="radio" id="${options[value]}" name="${spring.status.expression}" value="${value}" ${required}
                <#if spring.stringStatusValue == value>checked="checked"</#if> ${attributes}
            <@spring.closeTag/>
            <span class="label"><@u.message "${options[value]}" /></span>
        </label>
    </#list>
</#macro>

<#--
 * municipalitySelect
 *
 * Show a selectbox (dropdown) input element allowing a single value to be chosen
 * from a list of options.
 *
 * @param path the name of the field to bind to
 * @param options a map (value=label) of all the available options
 * @param attributes any additional attributes for the element (such as class
 *        or CSS styles or size
 * @param preSelected the predefined value for the select
 * @param multiple if allows selecting several munipalities
-->
<#macro municipalitySelect path options required="" cssClass="" attributes="" preSelected="" showLabel=true defaultOption="initiative.chooseMunicipality" allowSingleDeselect=false key="" onlyActive=false multiple=true id="">
    <@spring.bind path />
    
    <#if showLabel>
        <@formLabel path required false key />
    </#if>
    
    <@showError />
    
    <#--
     * This is crucial for JavaScript. It sets correct value both in double selects and single selects
     * with pre selected values and when error occurs in the form.
    -->
    <#if preSelected?string != "">
        <#assign data = preSelected />
    <#else>
        <#assign data = spring.status.value!"" />
    </#if>

    <select name="${spring.status.expression}" <#if id=="">id="${spring.status.expression}" <#else> id="${id}"  </#if> ${attributes} ${required} class="${cssClass} chzn-select" data-initiative-municipality="${data}" data-placeholder="<@u.message defaultOption />" <#if allowSingleDeselect>data-allow-single-deselect="allow"</#if> <#if multiple> multiple</#if>>
        <option value=""><@u.message defaultOption /></option>
        <#list options as option>
        <#if !onlyActive || option.active>
            <option value="${option.id}"<@checkSelected option.id preSelected />>${option.getName(locale)}</option>
        </#if>
        </#list>
    </select>
</#macro>

<#--
 * checkSelected
 *
 * Check a value in a list to see if it is the currently selected value.
 * If so, add the 'selected="selected"' text to the output.
 * Handles values of numeric and string types. Handles also sequences but only with spring.status.actualValue. Preselected option can't be list.
 * This function is used internally but can be accessed by user code if required.
 *
 * @param value the current value in a list iteration
 * @param preSelected option. If spring.status.value has value select it. Preselected option can't be list.
-->
<#macro checkSelected value preSelected>
    <#if spring.stringStatusValue?has_content>
        <#if spring.stringStatusValue?is_number && spring.stringStatusValue == value?number>selected="selected"</#if>
        <#if spring.stringStatusValue?is_string && spring.stringStatusValue == value?string>selected="selected"</#if>
        <#if spring.status.actualValue?is_sequence && spring.contains(spring.status.actualValue?default([""]), value) >selected="selected"</#if>
    <#else>
        <#if preSelected?is_number && value?is_number && preSelected == value?number>selected="selected"</#if>
        <#if preSelected?is_string && preSelected == value?string>selected="selected"</#if>
    </#if>
</#macro>


<#--
 * fieldRequiredInfo
 *
 * General infotext for required fields
 *
-->
<#macro fieldRequiredInfo>
    <@u.message "initiative.required" /> <span class="icon-small required"></span>
</#macro>

<#--
 * securityFilters
 *
 *  - CSRFToken
 *  - email-honeyspot field is hided from users with CSS thus they will leave it empty.
 *    Spambots will most propably fill the email field and humans should not.
-->
<#macro securityFilters honeySpot=true>
    <input type="hidden" name="CSRFToken" value="${CSRFToken!""}"/>
    <#if honeySpot><input type="text" name="email" id="email" tabindex="-1" class="noprint" /></#if>
</#macro>

<#--
 * notTooFastField
 *
 * Adds a timer for the form. If the form is submitted faster than 10 seconds
 * from the page load we will assume that the form is then filled by a bot and
 * a spam-filter validation error will occur. 
 *
 * @param object is initiative
-->
<#macro notTooFastField object>
    <input type="hidden" name="randomNumber" value="${object.randomNumber}"/>
</#macro>


<#--
 * cookieWarning
 *
 * Shows warning if not sure if user accepts cookies
 *
-->
<#macro cookieWarning currentPage>
    <#if cookieError??>
        <#assign cookieErrorHTML>
            <h4><@u.message "warning.cookieError.title" /></h4>
            <div><@u.messageHTML key="warning.cookieError.description" args=[currentPage] /></div>
        </#assign>
    
        <@u.systemMessageHTML cookieErrorHTML "warning" />
    </#if>
</#macro>


<#--
 * helpText
 *
 * Help texts for the edit-form
 *
 * @param path the name of the field to bind to
 * @param noscript use 'noscript' if message for noscript-users
-->
<#macro helpText path href="">
    <h4><@u.message path+".title" /></h4>
    <@u.messageHTML key=path+".description" args=[href] />
</#macro>


<#--
 * contactInfo
 *
 * Prints the edit block for current author's roles and contact details
 *
 * @param path is a string eg. "initiative"
 * @param mode is either 'modal' or 'full'
 * @param prefix for custom messages
 * @param cssClass for styling. Multiple classes are separated with a space

-->
<#macro contactInfo path disableEmail=true mode="" prefix="" cssClass="">

    <div class="initiative-contact-details">
        <div class="column col-1of2">
            
            <#-- Updating email is disabled -->
            <#if disableEmail>
                <@spring.bind path+".email" />
                <input type="hidden" name="contactInfo.email" value="${spring.status.value!""}" />
                <label class="input-header"><@u.message "contactInfo.email" /></label>
                <input type="text" disabled="disabled" class="medium disabled" value="${spring.status.value!""}" />
            <#else>
                <@textField path=path+".email" required="required" optional=false cssClass="medium" attributes='data-type="email"' maxLength=InitiativeConstants.CONTACT_EMAIL_MAX key="contactInfo.email" />
            </#if>
            
            <@textField path=path+".phone" required="" optional=false cssClass="medium"  maxLength=InitiativeConstants.CONTACT_PHONE_MAX key="contactInfo.phone" />
        </div>
        
        <div class="column col-1of2 last">
            <label class="input-header" for="contactInfo.address">
                <@u.message "contactInfo.address" />
            </label>
            <@spring.formTextarea path+".address", 'class="address-field noresize" maxlength="'+InitiativeConstants.CONTACT_ADDRESS_MAX+'"' />
        </div>
    
    </div>
        
</#macro>

<#macro verifiedContactInfo path prefix="" cssClass="">

    <div class="initiative-contact-details">
        <div class="column col-1of2">
            <@textField path=path+".email" required="required" optional=false cssClass="medium" attributes='data-type="email"' maxLength=InitiativeConstants.CONTACT_EMAIL_MAX key="contactInfo.email" />
            <@textField path=path+".phone" required="" optional=false cssClass="medium"  maxLength=InitiativeConstants.CONTACT_PHONE_MAX key="contactInfo.phone" />
        </div>

        <div class="column col-1of2 last">
            <label class="input-header" for="contactInfo.address">
                <@u.message "contactInfo.address" />
            </label>
            <@spring.formTextarea path+".address", 'class="address-field noresize" maxlength="'+InitiativeConstants.CONTACT_ADDRESS_MAX+'"' />
        </div>

    </div>

</#macro>

<#macro mobileCheckBox path prefix name value id cssClass="">
    <input type="radio" id=${id} name=${name} value=${value}
        <#if (path == value)>
           checked
        </#if>>
    </input>
    <label for=${id}><@u.message key=prefix+".mobile.checkbox" /></label>
</#macro>

</#escape> 
