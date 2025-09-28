import React, { useEffect, useState } from "react";

interface DynamicIconProps {
  iconName?: string;
  [key: string]: any;
}

// 缓存图标组件
const iconCache = new Map<string, React.ElementType>();

/* ================================
 * 动态加载 Ant Design 图标
 * ================================ */
export const DynamicAntdIcon: React.FC<DynamicIconProps> = ({ iconName, ...props }) => {
  const [Icon, setIcon] = useState<React.ElementType | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!iconName) {
      setIcon(null);
      return;
    }

    // 如果缓存中有，直接使用
    if (iconCache.has(iconName)) {
      setIcon(iconCache.get(iconName)!);
      return;
    }

    // 动态导入图标包
    import(/* @vite-ignore */ "@ant-design/icons")
      .then((module) => {
        const IconComponent = (module as any)[iconName];
        
        if (IconComponent && (typeof IconComponent === 'function' || IconComponent.$$typeof)) {
          iconCache.set(iconName, IconComponent);
          setIcon(() => IconComponent);
        } else {
          setError(`Icon "${iconName}" not found`);
        }
      })
      .catch((err) => {
        console.error(`Failed to load icon "${iconName}":`, err);
        setError(`Failed to load icon "${iconName}"`);
      });
  }, [iconName]);

  if (error) {
    console.warn(error);
    return null;
  }

  return Icon ? <Icon {...props} /> : null;
};

/* ================================
 * 同步获取已缓存的图标组件
 * ================================ */
export const getAntdIcon = (iconName: string, props?: any): React.ReactElement | null => {
  if (!iconName) return null;
  if (iconCache.has(iconName)) {
    const IconComponent = iconCache.get(iconName)!;
    return <IconComponent {...props} />;
  }
  return null; // 未缓存，需要异步加载
};

/* ================================
 * 检查图标是否已缓存
 * ================================ */
export const hasAntdIcon = (iconName: string): boolean => {
  return iconCache.has(iconName);
};

/* ================================
 * 预加载常用图标
 * ================================ */
export const preloadCommonIcons = async (
  iconNames: string[] = [
    "HomeOutlined",
    "SettingOutlined",
    "UserOutlined",
    "SearchOutlined",
    "PlusOutlined",
    "EditOutlined",
    "DeleteOutlined",
    "CheckOutlined",
    "CloseOutlined",
    "LoadingOutlined"
  ]
) => {
  const loadPromises = iconNames.map(async (iconName) => {
    if (!iconCache.has(iconName)) {
      try {
        const module = await import(/* @vite-ignore */ "@ant-design/icons");
        const IconComponent = (module as any)[iconName];
        if (IconComponent && (typeof IconComponent === 'function' || IconComponent.$$typeof)) {
          iconCache.set(iconName, IconComponent);
        }
      } catch (error) {
        console.warn(`Failed to preload icon "${iconName}":`, error);
      }
    }
  });

  await Promise.all(loadPromises);
};