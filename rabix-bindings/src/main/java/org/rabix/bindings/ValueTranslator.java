package org.rabix.bindings;

public interface ValueTranslator {

  Object translateToNative(Object commonValue) throws BindingException;
  
  Object translateToCommon(Object nativeValue) throws BindingException;
  
}
