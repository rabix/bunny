package org.rabix.bindings.sb.processor.callback;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.rabix.bindings.model.ApplicationPort;
import org.rabix.bindings.model.FileValue;
import org.rabix.bindings.sb.SBProcessor;
import org.rabix.bindings.sb.bean.SBInputPort;
import org.rabix.bindings.sb.bean.SBJob;
import org.rabix.bindings.sb.helper.SBFileValueHelper;
import org.rabix.bindings.sb.helper.SBSchemaHelper;
import org.rabix.bindings.sb.processor.SBPortProcessorCallback;
import org.rabix.bindings.sb.processor.SBPortProcessorResult;
import org.rabix.common.helper.ChecksumHelper.HashAlgorithm;
import org.rabix.common.helper.CloneHelper;

public class SBInputSecondaryFilesProcessor implements SBPortProcessorCallback {

  private SBJob job;
  private HashAlgorithm hashAlgorithm;

  public SBInputSecondaryFilesProcessor(SBJob job, HashAlgorithm hashAlgorithm) {
    this.job = job;
    this.hashAlgorithm = hashAlgorithm;
  }

  private boolean isFile(Object value){
    return SBSchemaHelper.isFileFromValue(value) || FileValue.isFileValue(value) ;
  }
  
  public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor)
  {
      Map<Object, Boolean> map = new ConcurrentHashMap<>();
      return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public SBPortProcessorResult process(Object value, String id, Object schema, Object binding, ApplicationPort parentPort) throws Exception {
    if (isFile(value) && parentPort instanceof SBInputPort) {
      if (!CollectionUtils.isEmpty(SBFileValueHelper.getSecondaryFiles(value))) {
        Map<String, Object> clonedValue = (Map<String, Object>) CloneHelper.deepCopy(value);
        List<Map<String, Object>> inputSecondaryFiles = SBProcessor.getSecondaryFiles(job, hashAlgorithm, clonedValue, SBFileValueHelper.getPath(clonedValue), binding);
        if(inputSecondaryFiles == null) {
          return new SBPortProcessorResult(value, false);
        }
        else {
          List<Map<String, Object>> result = Stream.concat(SBFileValueHelper.getSecondaryFiles(clonedValue).stream(), inputSecondaryFiles.stream()).filter(distinctByKey(p -> SBFileValueHelper.getPath(p))).collect(Collectors.toList());
          SBFileValueHelper.setSecondaryFiles(result, clonedValue);
          return new SBPortProcessorResult(clonedValue, true);
        }
      }
      
      List<Map<String, Object>> secondaryFiles = SBFileValueHelper.getSecondaryFiles(binding);
      if (secondaryFiles == null) {
        return new SBPortProcessorResult(value, false);
      }

      Map<String, Object> clonedValue = (Map<String, Object>) CloneHelper.deepCopy(value);      
      List<Map<String, Object>> out = SBProcessor.getSecondaryFiles(job, hashAlgorithm, clonedValue, SBFileValueHelper.getPath(clonedValue), binding);

      if (secondaryFiles != null) {
        SBFileValueHelper.setSecondaryFiles(out, clonedValue);
        return new SBPortProcessorResult(clonedValue, true);
      }
    }
    return new SBPortProcessorResult(value, false);
  }
}