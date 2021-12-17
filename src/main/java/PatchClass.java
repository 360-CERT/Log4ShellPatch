/**
 * @Classname PatchClass
 * @Description Patch the log4j 2, to set nolookups to true
 * @Author Hu3sky
 */

public class PatchClass {
    static {
        try {
            patch();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void patch() throws Exception{
        org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger();
        org.apache.logging.log4j.core.config.LoggerConfig loggerConfig =  ((org.apache.logging.log4j.core.Logger)logger).get();
        java.lang.reflect.Field config = null;
        try {
            Class configClass = loggerConfig.getClass();
            config = getField(configClass,config);
        } catch (Exception e) {
            System.out.println(e);
        }
        config.setAccessible(true);
        org.apache.logging.log4j.core.config.Configuration configuration = null;
        try {
            configuration = (org.apache.logging.log4j.core.config.Configuration) config.get(loggerConfig);
        } catch (IllegalAccessException e) {
            System.out.println(e);
        }
        java.util.Map appenders = configuration.getAppenders();
        java.util.Iterator iterator = appenders.entrySet().iterator();
        while (iterator.hasNext()){
            java.util.Map.Entry console = (java.util.Map.Entry) iterator.next();
            //判断appender类型
            org.apache.logging.log4j.core.appender.AbstractAppender appender = (org.apache.logging.log4j.core.appender.AbstractAppender) console.getValue();
            org.apache.logging.log4j.core.layout.PatternLayout layout = (org.apache.logging.log4j.core.layout.PatternLayout) appender.getLayout();
            if (layout == null){
                continue;
            }
            //2.11.0版本之前没有getEventSerializer方法
            //Object serializer = layout.getEventSerializer();
            java.lang.reflect.Field eventSerializer = layout.getClass().getDeclaredField("eventSerializer");
            eventSerializer.setAccessible(true);
            Object serializer = eventSerializer.get(layout);
            java.lang.reflect.Field formatters = null;
            try {
                formatters = serializer.getClass().getDeclaredField("formatters");
            } catch (NoSuchFieldException e) {
                System.out.println(e);
            }
            formatters.setAccessible(true);
            org.apache.logging.log4j.core.pattern.PatternFormatter[] patterns = new org.apache.logging.log4j.core.pattern.PatternFormatter[0];
            try {
                patterns = (org.apache.logging.log4j.core.pattern.PatternFormatter[]) formatters.get(serializer);
            } catch (IllegalAccessException e) {
                System.out.println(e);
            }
            try {
                getConverter(patterns);
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        System.out.println("   _____ _____ ____  ________________  ______\n" +
                "  |__  // ___// __ \\/ ____/ ____/ __ \\/_  __/\n" +
                "   /_ </ __ \\/ / / / /   / __/ / /_/ / / /   \n" +
                " ___/ / /_/ / /_/ / /___/ /___/ _, _/ / /    \n" +
                "/____/\\____/\\____/\\____/_____/_/ |_| /_/     \n" +
                "                                             ");
        System.out.println("[+] It works ! Already set all noLookups to true");
    }

    public static void getConverter(org.apache.logging.log4j.core.pattern.PatternFormatter[] patternFormatters) {
        for (org.apache.logging.log4j.core.pattern.PatternFormatter formatter : patternFormatters) {
            if (formatter.getConverter() instanceof org.apache.logging.log4j.core.pattern.MessagePatternConverter) {
                java.lang.reflect.Field noLookups = null;
                try {
                    noLookups = formatter.getConverter().getClass().getDeclaredField("noLookups");
                } catch (NoSuchFieldException e) {
                    System.out.println(e);
                }
                noLookups.setAccessible(true);
                try {
                    noLookups.set(formatter.getConverter(),true);
                } catch (IllegalAccessException e) {
                    System.out.println(e);
                }
                System.out.println("[+] Successful set noLookups to true !!!");
                break;
            }
            else {
                //formatter.getConverter() = MaxLengthConverter;
                java.lang.reflect.Field[] fields = formatter.getConverter().getClass().getDeclaredFields();
                for (java.lang.reflect.Field field : fields){
                    if ("formatters".equals(field.getName()) || "patternFormatters".equals(field.getName())) {
                        //获取formatters
                        field.setAccessible(true);
                        //StyleConverter -> private final List<PatternFormatter> patternFormatters;
                        //EncodingPatternConverter -> private final List<PatternFormatter> formatters;
                        //HighlightConverter ->  private final List<PatternFormatter> patternFormatters;
                        //EqualsBaseReplacementConverter -> private final List<PatternFormatter> substitutionFormatters;
                        //AbstractStyleNameConverter -> private final List<PatternFormatter> formatters;
                        //VariablesNotEmptyReplacementConverter -> private final List<PatternFormatter> formatters;
                        //ThrowablePatternConverter -> protected final List<PatternFormatter> formatters;
                        //RegexReplacementConverter -> private final List<PatternFormatter> formatters;
                        //MaxLengthConverter -> private final List<PatternFormatter> formatters;
                        java.util.List<org.apache.logging.log4j.core.pattern.PatternFormatter> formatters;
                        try {
                            formatters = (java.util.List<org.apache.logging.log4j.core.pattern.PatternFormatter>) field.get(formatter.getConverter());
                            if (formatters != null) {
                                getListFomatters(formatters);
                            }
                        } catch (IllegalAccessException e) {
                            System.out.println(e);
                        }
                    }
                }
            }
        }
    }

    public static void getListFomatters(java.util.List<org.apache.logging.log4j.core.pattern.PatternFormatter> formatters)  {
        for (int i = 0; i < formatters.size(); ++i) {
            org.apache.logging.log4j.core.pattern.PatternFormatter nestFormatter = formatters.get(i);
            //System.out.println(nestFormatter.getConverter());
            if (nestFormatter.getConverter() instanceof org.apache.logging.log4j.core.pattern.MessagePatternConverter) {
                java.lang.reflect.Field noLookups = null;
                try {
                    noLookups = nestFormatter.getConverter().getClass().getDeclaredField("noLookups");
                } catch (NoSuchFieldException e) {
                    System.out.println(e);
                }
                noLookups.setAccessible(true);
                try {
                    noLookups.set(nestFormatter.getConverter(), true);
                } catch (IllegalAccessException e) {
                    System.out.println(e);
                }
                System.out.println("[+] Successful set noLookups to true !!!");
            } else {
                java.lang.reflect.Field[] fields = nestFormatter.getConverter().getClass().getDeclaredFields();
                for (java.lang.reflect.Field field : fields) {
                    if ("formatters".equals(field.getName()) || "patternFormatters".equals(field.getName())) {
                        field.setAccessible(true);
                        java.util.List<org.apache.logging.log4j.core.pattern.PatternFormatter> nestFormatters = null;
                        try {
                            nestFormatters = (java.util.List<org.apache.logging.log4j.core.pattern.PatternFormatter>) field.get(nestFormatter.getConverter());
                            if (nestFormatters != null){
                                getListFomatters(nestFormatters);
                            }
                        } catch (IllegalAccessException e) {
                            System.out.println(e);
                        }
                    }
                }
            }
        }
    }

    public static java.lang.reflect.Field getField(java.lang.Class configClass,java.lang.reflect.Field config){
        if(!configClass.getName().equals("java.lang.Object")) {
            java.lang.reflect.Field[] fields = configClass.getDeclaredFields();
            for (java.lang.reflect.Field field : fields){
                if ("config".equals(field.getName())) {
                    try {
                        config = configClass.getDeclaredField("config");
                        break;
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
            }
            if (config == null){
                config = getField(configClass.getSuperclass(),config);
            }
        }else {
            return null;
        }
        return config;
    }
}
