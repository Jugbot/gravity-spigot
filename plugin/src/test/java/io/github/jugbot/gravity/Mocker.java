package io.github.jugbot.gravity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.bukkit.World;
import org.apache.commons.lang.NotImplementedException;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

public class Mocker {
  static <T> T newMock(Class<T> c) {
    ProxyFactory factory = new ProxyFactory();
    factory.setSuperclass(c);
    factory.setFilter(
        new MethodFilter() {
          @Override
          public boolean isHandled(Method method) {
            return Modifier.isAbstract(method.getModifiers());
          }
        });

    try {
      return (T)
          factory.create(
              new Class<?>[0],
              new Object[0],
              new MethodHandler() {
                @Override
                public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
                  throw new NotImplementedException();
                }
              });
    } catch (NoSuchMethodException
        | IllegalArgumentException
        | InstantiationException
        | IllegalAccessException
        | InvocationTargetException e) {
      e.printStackTrace();
      return null;
    }
  }

  static <T> Class<T> newMockClass(Class<T> c) {
    ProxyFactory factory = new ProxyFactory();
    factory.setSuperclass(c);
    factory.setFilter(
        new MethodFilter() {
          @Override
          public boolean isHandled(Method method) {
            return Modifier.isAbstract(method.getModifiers());
          }
        });

    return factory.createClass();
  }
}
