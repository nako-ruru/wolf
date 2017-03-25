本工程提供websocket服务，使用方式如下：

1.声明提供客户端连接路径的ServerEndpoint
    @ServerEndpoint(value="/message", configurator=ServletAwareConfig.class)
    public class MessageEndpoint extends AbstractEndpoint {
    }

2.提供一个处理器，通过@WebSocketController注解该类，并保证该类在ServletContext里的ApplicationContext里面；通过
  @WebSocketRequestMapping注解处理逻辑的方法，其参数action为json的action属性，方法的参数可以是HttpSession, Session，也可以是
  json里面的属性
    /**
     *
     * @author hehj
     */
    @RestController
    @WebSocketController
    public class SubscribeController {

        private static final Logger LOG = LoggerFactory.getLogger(SubscribeController.class);

        @Resource
        private MarketServer marketServer;

        @RequestMapping(value = "subscribe/{securityIds}", method = RequestMethod.POST)
        public void subscribe(HttpSession session, @PathVariable String securityIds) {
            String[] securityIdArray = split(securityIds);
            subscribe(session, securityIdArray);
        }

        @WebSocketRequestMapping(action = Behaviour.ACTION_SUBSCRIBE)
        public void subscribe(HttpSession session, String... securityIds) {
            marketServer.subscribe(session.getId(), securityIds);
        }

        @RequestMapping(value = "unsubscribe/{securityIds}", method = RequestMethod.POST)
        public void unsubscribe(HttpSession session, @PathVariable String securityIds) {
            String[] securityIdArray = split(securityIds);
            unsubscribe(session, securityIdArray);
        }

        @WebSocketRequestMapping(action = Behaviour.ACTION_UNSUBSCRIBE)
        public void unsubscribe(HttpSession session, String... securityIds) {
            marketServer.unSubscribe(session.getId(), securityIds);
        }

        private static String[] split(String securityIds) {
            String[] securityIdArray = securityIds.split(",");
            for(int i = 0; i < securityIdArray.length; i++) {
                securityIdArray[i] = securityIdArray[i].trim();
            }
            return securityIdArray;
        }

    }
