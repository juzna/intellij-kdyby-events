<?php
namespace {
    class EventManager extends \Doctrine\Common\EventManager implements \Symfony\Component\EventDispatcher\EventDispatcherInterface
    {
        public function dispatch($eventName)
        {

        }

    }

}

namespace Symfony\Component\EventDispatcher {
    interface EventDispatcherInterface {

        public function dispatch($eventName);
    }
}

namespace Doctrine\Common {

    class EventManager {

        public function dispatchEvent($eventName)
        {

        }
    }
}