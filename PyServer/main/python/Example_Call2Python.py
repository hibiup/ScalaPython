"""
需要安装 PY4J:
~~~
$ python -m pip install py4j
~~~
"""

"""
1）实现  Java/Scala 的 interface/trait
"""


class PythonImpl(object):
    class Java:
        """
        声明实现的 interface
        """
        implements = ["com.hibiup.examples.PythonHello"]

    def sayHello(self, param):
        # 本地打印
        print("Python receive message: {0}".format(param))

        # GatewayServer 和 JavaGateway 具有双向交流能力，因此可以执行在客户端的输出。
        gateway.jvm.System.out.println("[" + gateway.jvm.Thread.currentThread().getName() + "] - " + param)  # JVM 打印

        # 返回给客户端
        return "Hello, {0}".format(param)


"""
2) 必须先于 JVM 启动 Python 程序.
"""
if __name__ == "__main__":
    # 生成 Python 端的服务实例
    impl = PythonImpl()

    # JavaGateway 会打开一个侦听端口用于接受来自客户端（ClientServer 或 GatewayServer）的注册请求。缺省端口是 25334
    # JavaGateway 似乎存在 Bug, 只能向 JVM 提交一次请求，因此建议使用 ClientServer
    from py4j.java_gateway import JavaGateway, CallbackServerParameters
    #ateway = JavaGateway(
    #    callback_server_parameters=CallbackServerParameters(),
    #    python_server_entry_point=impl  # 指定服务实例
    #)

    # ClientServer 可以实现多次双向交流，但是 ClientServer 的客户端也必须是 ClientServer，不能接入 GatewayServer
    from py4j.clientserver import ClientServer, JavaParameters, PythonParameters
    gateway = ClientServer(
        java_parameters=JavaParameters(),
        python_parameters=PythonParameters(),
        python_server_entry_point=impl)
