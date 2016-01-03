package com.assabetsecurity.core.validation

import org.slf4s.Logging
import com.assabetsecurity.core.data.{HtmlValidationClass, SiteConfig, ValidationClass, IpValidationClass}

/**
 * User: alyas
 * Date: 8/18/13
 * Time: 11:24 PM
 */
trait ValidationClassProcessor extends Logging {
  def validate(m:SiteMessageContent, data:MessageValidationActorData, siteConfig:SiteConfig):List[ValidationResult]

  def getValidationClass[T <: ValidationClass : Manifest](config:SiteConfig):List[T] = {
    val t = manifest[T]
      config.validationClass.filter(v=>
       t.equals(manifest[v.type]) && v.enabled).asInstanceOf[List[T]]
  }
}



