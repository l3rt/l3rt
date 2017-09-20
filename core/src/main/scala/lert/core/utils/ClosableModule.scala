package lert.core.utils

import com.google.inject.Injector

trait ClosableModule {
  def close(injector: Injector)
}
